package com.app.gerencia.controllers;

import com.app.gerencia.controllers.dto.appointment.*;
import com.app.gerencia.entities.Appointment;
import com.app.gerencia.enums.AppointmentStatus;
import com.app.gerencia.enums.FrequencyStatus;
import com.app.gerencia.services.AppointmentService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

// Agendamentos (únicos e recorrentes). Cada profissional só enxerga/altera os seus próprios
// agendamentos — o admin tem visão total e pode filtrar por profissional/paciente/período.
@RestController
@RequestMapping("/api-gateway/gerencia/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PreAuthorize("hasAuthority('SCOPE_PROFESSIONAL')")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateAppointmentRequestDTO dto) {
        try {
            Appointment saved = appointmentService.create(dto, currentUserId());
            return ResponseEntity.status(HttpStatus.CREATED).body(AppointmentDTO.fromEntity(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('SCOPE_PROFESSIONAL', 'SCOPE_ADMIN')")
    @GetMapping
    public ResponseEntity<List<AppointmentDTO>> list(
            @RequestParam(required = false) Long professionalId,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) String status
    ) {
        AppointmentStatus statusFilter = parseAppointmentStatus(status);
        List<AppointmentDTO> dtos = appointmentService
                .search(currentUserId(), isAdmin(), professionalId, patientId, statusFilter)
                .stream().map(AppointmentDTO::fromEntity).toList();
        return ResponseEntity.ok(dtos);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('SCOPE_PROFESSIONAL', 'SCOPE_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            Appointment appointment = appointmentService.findById(id);
            assertOwnershipOrAdmin(appointment);
            return ResponseEntity.ok(AppointmentDTO.fromEntity(appointment));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    // Cancelamento com escopo (RB08 / seção 16): THIS | FROM_HERE | ALL — nunca apaga histórico
    @PreAuthorize("hasAnyAuthority('SCOPE_PROFESSIONAL', 'SCOPE_ADMIN')")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id, @RequestBody CancelAppointmentRequestDTO dto) {
        try {
            appointmentService.cancel(id, dto, currentUserId(), isAdmin());
            return ResponseEntity.ok(AppointmentDTO.fromEntity(appointmentService.findById(id)));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Agenda/calendário: todas as ocorrências (Frequency) no período — alimenta o calendário mensal
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('SCOPE_PROFESSIONAL', 'SCOPE_ADMIN')")
    @GetMapping("/calendar")
    public ResponseEntity<?> calendar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long professionalId,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) String status
    ) {
        if (endDate.isBefore(startDate)) {
            return ResponseEntity.badRequest().body("A data final não pode ser anterior à data inicial");
        }
        FrequencyStatus statusFilter = parseFrequencyStatus(status);
        List<FrequencyDTO> dtos = appointmentService
                .searchFrequencies(currentUserId(), isAdmin(), professionalId, patientId, statusFilter, startDate, endDate)
                .stream().map(FrequencyDTO::fromEntity).toList();
        return ResponseEntity.ok(dtos);
    }

    // Resumo do período (dashboard) — sempre calculado a partir dos dados reais
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('SCOPE_PROFESSIONAL', 'SCOPE_ADMIN')")
    @GetMapping("/summary")
    public ResponseEntity<?> summary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long professionalId,
            @RequestParam(required = false) Long patientId
    ) {
        if (endDate.isBefore(startDate)) {
            return ResponseEntity.badRequest().body("A data final não pode ser anterior à data inicial");
        }
        AppointmentSummaryDTO summary = appointmentService
                .summary(currentUserId(), isAdmin(), professionalId, patientId, startDate, endDate);
        return ResponseEntity.ok(summary);
    }

    private void assertOwnershipOrAdmin(Appointment appointment) {
        if (isAdmin()) return;
        if (!appointment.getProfessional().getId().equals(currentUserId())) {
            throw new AccessDeniedException("Você não tem permissão para visualizar este agendamento");
        }
    }

    private AppointmentStatus parseAppointmentStatus(String status) {
        if (status == null || status.isBlank()) return null;
        try {
            return AppointmentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private FrequencyStatus parseFrequencyStatus(String status) {
        if (status == null || status.isBlank()) return null;
        try {
            return FrequencyStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Long currentUserId() {
        return Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("SCOPE_ADMIN"));
    }
}
