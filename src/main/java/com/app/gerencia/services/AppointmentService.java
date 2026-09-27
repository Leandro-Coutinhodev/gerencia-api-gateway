package com.app.gerencia.services;

import com.app.gerencia.controllers.dto.appointment.AppointmentSummaryDTO;
import com.app.gerencia.controllers.dto.appointment.CancelAppointmentRequestDTO;
import com.app.gerencia.controllers.dto.appointment.CreateAppointmentRequestDTO;
import com.app.gerencia.entities.*;
import com.app.gerencia.enums.AppointmentStatus;
import com.app.gerencia.enums.AppointmentType;
import com.app.gerencia.enums.FrequencyStatus;
import com.app.gerencia.enums.RecurrenceFrequencyType;
import com.app.gerencia.repository.AppointmentRepository;
import com.app.gerencia.repository.FrequencyRepository;
import com.app.gerencia.repository.ProfessionalRepository;
import com.app.gerencia.repository.RecordRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

// Agendamento (Appointment) + geração das ocorrências (Frequency). Isolamento por profissional
// é sempre resolvido a partir do usuário autenticado (nunca confiando em professionalId do cliente) —
// ver AppointmentController/currentUserId().
@Service
public class AppointmentService {

    private static final int DEFAULT_DURATION_MINUTES = 50;
    private static final int MAX_OCCURRENCES = 366;

    private final AppointmentRepository appointmentRepository;
    private final FrequencyRepository frequencyRepository;
    private final RecordRepository recordRepository;
    private final ProfessionalRepository professionalRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              FrequencyRepository frequencyRepository,
                              RecordRepository recordRepository,
                              ProfessionalRepository professionalRepository) {
        this.appointmentRepository = appointmentRepository;
        this.frequencyRepository = frequencyRepository;
        this.recordRepository = recordRepository;
        this.professionalRepository = professionalRepository;
    }

    @Transactional
    public Appointment create(CreateAppointmentRequestDTO dto, Long professionalId) {
        if (dto.attendanceRecordId() == null)
            throw new IllegalArgumentException("Selecione a ficha de atendimento (RB02)");
        if (dto.time() == null)
            throw new IllegalArgumentException("Informe o horário do atendimento");

        AttendanceRecord attendanceRecord = recordRepository.findById(dto.attendanceRecordId())
                .orElseThrow(() -> new EntityNotFoundException("Ficha de atendimento não encontrada"));

        // RB01/RB06: a ficha (e, por consequência, o agendamento) precisa pertencer ao profissional autenticado
        if (!attendanceRecord.getProfessional().getId().equals(professionalId)) {
            throw new AccessDeniedException("Esta ficha de atendimento pertence a outro profissional");
        }

        Professional professional = professionalRepository.findById(professionalId)
                .orElseThrow(() -> new EntityNotFoundException("Profissional não encontrado"));
        Patient patient = attendanceRecord.getPatient(); // RB03: paciente do agendamento = paciente da ficha

        AppointmentType type = parseType(dto.type());
        int duration = dto.durationMinutes() != null && dto.durationMinutes() > 0
                ? dto.durationMinutes() : DEFAULT_DURATION_MINUTES;

        Appointment appointment = new Appointment();
        appointment.setProfessional(professional);
        appointment.setPatient(patient);
        appointment.setAttendanceRecord(attendanceRecord);
        appointment.setType(type);
        appointment.setTime(dto.time());
        appointment.setDurationMinutes(duration);
        appointment.setNotes(dto.notes());
        appointment.setStatus(AppointmentStatus.ATIVO);

        List<LocalDate> occurrenceDates;
        if (type == AppointmentType.UNICO) {
            if (dto.date() == null)
                throw new IllegalArgumentException("Informe a data do agendamento");
            appointment.setDate(dto.date());
            occurrenceDates = List.of(dto.date());
        } else {
            AppointmentRecurrence recurrence = buildRecurrence(dto.recurrence());
            appointment.setRecurrence(recurrence);
            occurrenceDates = generateOccurrenceDates(recurrence);
        }

        if (occurrenceDates.isEmpty())
            throw new IllegalArgumentException("A regra informada não gerou nenhuma data de atendimento");
        if (occurrenceDates.size() > MAX_OCCURRENCES)
            throw new IllegalArgumentException(
                    "O intervalo é muito longo (máximo de " + MAX_OCCURRENCES + " ocorrências). Reduza o período.");

        // RB10: verifica conflito de horário para TODAS as ocorrências antes de persistir qualquer coisa
        LocalTime start = dto.time();
        LocalTime end = start.plusMinutes(duration);
        List<LocalDate> conflicts = occurrenceDates.stream()
                .filter(d -> hasConflict(professionalId, d, start, end, null))
                .toList();
        if (!conflicts.isEmpty()) {
            String dates = conflicts.stream().map(LocalDate::toString).collect(Collectors.joining(", "));
            throw new IllegalStateException(
                    "Conflito de horário para o profissional nas seguintes datas: " + dates);
        }

        appointment = appointmentRepository.save(appointment);

        List<Frequency> occurrences = new ArrayList<>();
        for (LocalDate occurrenceDate : occurrenceDates) {
            Frequency f = new Frequency();
            f.setAppointment(appointment);
            f.setProfessional(professional);
            f.setPatient(patient);
            f.setAttendanceRecord(attendanceRecord);
            f.setScheduledDate(occurrenceDate);
            f.setScheduledTime(start);
            f.setDurationMinutes(duration);
            f.setStatus(FrequencyStatus.AGENDADO);
            occurrences.add(f);
        }
        frequencyRepository.saveAll(occurrences);

        return appointment;
    }

    @Transactional
    public void cancel(Long appointmentId, CancelAppointmentRequestDTO dto, Long requesterId, boolean isAdmin) {
        Appointment appointment = findById(appointmentId);
        assertOwnership(appointment, requesterId, isAdmin);

        String scope = dto.scope() == null ? "ALL" : dto.scope().toUpperCase();
        LocalDateTime now = LocalDateTime.now();

        switch (scope) {
            case "ALL" -> {
                appointment.setStatus(AppointmentStatus.CANCELADO);
                appointment.setUpdatedAt(now);
                appointmentRepository.save(appointment);

                // Cancela apenas as ocorrências ainda pendentes — nunca sobrescreve ocorrências
                // já resolvidas (PRESENTE/AUSENTE/JUSTIFICADO), preservando o histórico (RB09)
                List<Frequency> pending =
                        frequencyRepository.findByAppointmentIdAndStatus(appointmentId, FrequencyStatus.AGENDADO);
                pending.forEach(f -> markCancelled(f, now));
                frequencyRepository.saveAll(pending);
            }
            case "THIS" -> {
                // Ação explícita sobre UMA ocorrência específica escolhida pelo profissional —
                // pode ser aplicada independentemente do status atual dela
                Frequency occurrence = requireOwnOccurrence(appointmentId, dto.frequencyId());
                markCancelled(occurrence, now);
                frequencyRepository.save(occurrence);
                closeSeriesIfNoPendingOccurrenceRemains(appointment, now);
            }
            case "FROM_HERE" -> {
                Frequency anchor = requireOwnOccurrence(appointmentId, dto.frequencyId());
                List<Frequency> pending = frequencyRepository
                        .findByAppointmentIdAndScheduledDateGreaterThanEqualAndStatus(
                                appointmentId, anchor.getScheduledDate(), FrequencyStatus.AGENDADO);
                pending.forEach(f -> markCancelled(f, now));
                frequencyRepository.saveAll(pending);
                closeSeriesIfNoPendingOccurrenceRemains(appointment, now);
            }
            default -> throw new IllegalArgumentException("Escopo de cancelamento inválido: " + dto.scope());
        }
    }

    public Appointment findById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado com id: " + id));
    }

    // professionalIdFilter só é respeitado quando isAdmin=true; caso contrário, força o próprio profissional (RB06)
    public List<Appointment> search(Long requesterId, boolean isAdmin, Long professionalIdFilter,
                                    Long patientIdFilter, AppointmentStatus status) {
        Long professionalId = isAdmin ? professionalIdFilter : requesterId;
        return appointmentRepository.search(professionalId, patientIdFilter, status);
    }

    public List<Frequency> searchFrequencies(Long requesterId, boolean isAdmin, Long professionalIdFilter,
                                             Long patientIdFilter, FrequencyStatus status,
                                             LocalDate startDate, LocalDate endDate) {
        Long professionalId = isAdmin ? professionalIdFilter : requesterId;
        return frequencyRepository.search(professionalId, patientIdFilter, status, startDate, endDate);
    }

    public AppointmentSummaryDTO summary(Long requesterId, boolean isAdmin, Long professionalIdFilter,
                                         Long patientIdFilter, LocalDate startDate, LocalDate endDate) {
        List<Frequency> occurrences =
                searchFrequencies(requesterId, isAdmin, professionalIdFilter, patientIdFilter, null, startDate, endDate);

        Map<FrequencyStatus, Long> byStatus = occurrences.stream()
                .collect(Collectors.groupingBy(Frequency::getStatus, Collectors.counting()));

        return new AppointmentSummaryDTO(
                byStatus.getOrDefault(FrequencyStatus.AGENDADO, 0L),
                byStatus.getOrDefault(FrequencyStatus.PRESENTE, 0L),
                byStatus.getOrDefault(FrequencyStatus.AUSENTE, 0L),
                byStatus.getOrDefault(FrequencyStatus.JUSTIFICADO, 0L),
                byStatus.getOrDefault(FrequencyStatus.CANCELADO, 0L)
        );
    }

    // --- Helpers privados ---

    private AppointmentType parseType(String type) {
        if (type == null || type.isBlank())
            throw new IllegalArgumentException("Informe o tipo de agendamento (UNICO ou RECORRENTE)");
        try {
            return AppointmentType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de agendamento inválido: " + type);
        }
    }

    private AppointmentRecurrence buildRecurrence(CreateAppointmentRequestDTO.RecurrenceRequestDTO dto) {
        if (dto == null)
            throw new IllegalArgumentException("Informe a regra de recorrência (RB04)");
        if (dto.startDate() == null || dto.endDate() == null)
            throw new IllegalArgumentException("Informe a data inicial e a data final da recorrência (RB04)");
        if (dto.endDate().isBefore(dto.startDate()))
            throw new IllegalArgumentException("A data final não pode ser anterior à data inicial");
        if (dto.frequencyType() == null || dto.frequencyType().isBlank())
            throw new IllegalArgumentException("Informe a regra de recorrência (diária, semanal ou mensal)");

        RecurrenceFrequencyType freqType;
        try {
            freqType = RecurrenceFrequencyType.valueOf(dto.frequencyType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de recorrência inválido: " + dto.frequencyType());
        }

        AppointmentRecurrence recurrence = new AppointmentRecurrence();
        recurrence.setFrequencyType(freqType);
        recurrence.setStartDate(dto.startDate());
        recurrence.setEndDate(dto.endDate());

        if (freqType == RecurrenceFrequencyType.SEMANAL) {
            if (dto.daysOfWeek() == null || dto.daysOfWeek().isEmpty())
                throw new IllegalArgumentException("Selecione ao menos um dia da semana para a recorrência semanal");
            recurrence.setDaysOfWeek(dto.daysOfWeek().stream().map(String::valueOf)
                    .collect(Collectors.joining("|")));
        }
        return recurrence;
    }

    private List<LocalDate> generateOccurrenceDates(AppointmentRecurrence recurrence) {
        List<LocalDate> dates = new ArrayList<>();

        if (recurrence.getFrequencyType() == RecurrenceFrequencyType.MENSAL) {
            int dayOfMonth = recurrence.getStartDate().getDayOfMonth();
            YearMonth ym = YearMonth.from(recurrence.getStartDate());
            YearMonth endYm = YearMonth.from(recurrence.getEndDate());
            while (!ym.isAfter(endYm) && dates.size() <= MAX_OCCURRENCES) {
                LocalDate occurrence = ym.atDay(Math.min(dayOfMonth, ym.lengthOfMonth()));
                if (!occurrence.isBefore(recurrence.getStartDate()) && !occurrence.isAfter(recurrence.getEndDate())) {
                    dates.add(occurrence);
                }
                ym = ym.plusMonths(1);
            }
            return dates;
        }

        Set<DayOfWeek> allowedDays = parseDaysOfWeek(recurrence.getDaysOfWeek());
        LocalDate current = recurrence.getStartDate();
        while (!current.isAfter(recurrence.getEndDate()) && dates.size() <= MAX_OCCURRENCES) {
            boolean include = switch (recurrence.getFrequencyType()) {
                case DIARIA -> true;
                case SEMANAL -> allowedDays.contains(current.getDayOfWeek());
                case MENSAL -> false; // tratado acima
            };
            if (include) dates.add(current);
            current = current.plusDays(1);
        }
        return dates;
    }

    private Set<DayOfWeek> parseDaysOfWeek(String daysOfWeek) {
        if (daysOfWeek == null || daysOfWeek.isBlank()) return Set.of();
        return Arrays.stream(daysOfWeek.split("\\|"))
                .map(Integer::parseInt)
                .map(DayOfWeek::of)
                .collect(Collectors.toSet());
    }

    // RB10: verifica sobreposição de horário para o mesmo profissional na mesma data
    private boolean hasConflict(Long professionalId, LocalDate date, LocalTime start, LocalTime end,
                                Long excludeFrequencyId) {
        List<Frequency> existing = frequencyRepository
                .findByProfessionalIdAndScheduledDateAndStatusNot(professionalId, date, FrequencyStatus.CANCELADO);

        for (Frequency f : existing) {
            if (excludeFrequencyId != null && excludeFrequencyId.equals(f.getId())) continue;
            LocalTime existingStart = f.getScheduledTime();
            LocalTime existingEnd = existingStart.plusMinutes(
                    f.getDurationMinutes() != null ? f.getDurationMinutes() : DEFAULT_DURATION_MINUTES);
            if (existingStart.isBefore(end) && start.isBefore(existingEnd)) return true;
        }
        return false;
    }

    private Frequency requireOwnOccurrence(Long appointmentId, Long frequencyId) {
        if (frequencyId == null)
            throw new IllegalArgumentException("Informe a ocorrência de referência para este escopo de cancelamento");
        Frequency occurrence = frequencyRepository.findById(frequencyId)
                .orElseThrow(() -> new EntityNotFoundException("Ocorrência não encontrada"));
        if (!occurrence.getAppointment().getId().equals(appointmentId))
            throw new IllegalArgumentException("Esta ocorrência não pertence ao agendamento informado");
        return occurrence;
    }

    private void markCancelled(Frequency f, LocalDateTime now) {
        f.setStatus(FrequencyStatus.CANCELADO);
        f.setMarkedAt(now);
    }

    // Se não sobrou nenhuma ocorrência pendente (AGENDADO) na série, marca a série inteira como
    // cancelada também — sem mexer no status de ocorrências já resolvidas no passado
    private void closeSeriesIfNoPendingOccurrenceRemains(Appointment appointment, LocalDateTime now) {
        boolean anyPending = frequencyRepository
                .existsByAppointmentIdAndStatus(appointment.getId(), FrequencyStatus.AGENDADO);
        if (!anyPending && appointment.getStatus() != AppointmentStatus.CANCELADO) {
            appointment.setStatus(AppointmentStatus.CANCELADO);
            appointment.setUpdatedAt(now);
            appointmentRepository.save(appointment);
        }
    }

    private void assertOwnership(Appointment appointment, Long requesterId, boolean isAdmin) {
        if (isAdmin) return;
        if (!appointment.getProfessional().getId().equals(requesterId)) {
            throw new AccessDeniedException("Você não tem permissão para alterar este agendamento");
        }
    }
}
