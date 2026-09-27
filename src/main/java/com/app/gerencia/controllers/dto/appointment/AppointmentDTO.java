package com.app.gerencia.controllers.dto.appointment;

import com.app.gerencia.entities.Appointment;
import com.app.gerencia.entities.AppointmentRecurrence;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

public record AppointmentDTO(
        Long id,
        Long professionalId,
        String professionalName,
        Long patientId,
        String patientName,
        Long attendanceRecordId,
        String attendanceFormName,
        String type,
        String status,
        LocalDate date,
        LocalTime time,
        Integer durationMinutes,
        String notes,
        RecurrenceDTO recurrence,
        LocalDateTime createdAt,
        List<FrequencyDTO> frequencies
) {
    public record RecurrenceDTO(
            Long id, String frequencyType, List<Integer> daysOfWeek, LocalDate startDate, LocalDate endDate
    ) {
        static RecurrenceDTO fromEntity(AppointmentRecurrence r) {
            if (r == null) return null;
            List<Integer> days = r.getDaysOfWeek() != null && !r.getDaysOfWeek().isBlank()
                    ? Arrays.stream(r.getDaysOfWeek().split("\\|")).map(Integer::parseInt).toList()
                    : List.of();
            return new RecurrenceDTO(r.getId(), r.getFrequencyType().name(), days, r.getStartDate(), r.getEndDate());
        }
    }

    public static AppointmentDTO fromEntity(Appointment a) {
        var template = a.getAttendanceRecord().getTemplate();
        return new AppointmentDTO(
                a.getId(),
                a.getProfessional().getId(), a.getProfessional().getName(),
                a.getPatient().getId(), a.getPatient().getName(),
                a.getAttendanceRecord().getId(), template != null ? template.getName() : null,
                a.getType().name(), a.getStatus().name(),
                a.getDate(), a.getTime(), a.getDurationMinutes(), a.getNotes(),
                RecurrenceDTO.fromEntity(a.getRecurrence()),
                a.getCreatedAt(),
                a.getFrequencies().stream().map(FrequencyDTO::fromEntity).toList()
        );
    }
}
