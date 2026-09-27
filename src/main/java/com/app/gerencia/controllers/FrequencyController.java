package com.app.gerencia.controllers;

import com.app.gerencia.controllers.dto.appointment.FrequencyDTO;
import com.app.gerencia.controllers.dto.appointment.UpdateFrequencyStatusRequestDTO;
import com.app.gerencia.entities.Frequency;
import com.app.gerencia.enums.FrequencyStatus;
import com.app.gerencia.services.AppointmentService;
import com.app.gerencia.services.FrequencyService;
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

// Frequência: cada ocorrência concreta de atendimento e o registro de presença/ausência.
@RestController
@RequestMapping("/api-gateway/gerencia/frequencies")
public class FrequencyController {

    private final FrequencyService frequencyService;
    private final AppointmentService appointmentService;

    public FrequencyController(FrequencyService frequencyService, AppointmentService appointmentService) {
        this.frequencyService = frequencyService;
        this.appointmentService = appointmentService;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('SCOPE_PROFESSIONAL', 'SCOPE_ADMIN')")
    @GetMapping
    public ResponseEntity<List<FrequencyDTO>> list(
            @RequestParam(required = false) Long professionalId,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        LocalDate from = startDate != null ? startDate : LocalDate.now().minusYears(1);
        LocalDate to = endDate != null ? endDate : LocalDate.now().plusYears(1);
        FrequencyStatus statusFilter = parseStatus(status);

        List<FrequencyDTO> dtos = appointmentService
                .searchFrequencies(currentUserId(), isAdmin(), professionalId, patientId, statusFilter, from, to)
                .stream().map(FrequencyDTO::fromEntity).toList();
        return ResponseEntity.ok(dtos);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('SCOPE_PROFESSIONAL', 'SCOPE_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            Frequency f = frequencyService.findById(id, currentUserId(), isAdmin());
            return ResponseEntity.ok(FrequencyDTO.fromEntity(f));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    // Registro de presença/ausência/justificativa (RB09: nunca apaga, só muda status)
    @PreAuthorize("hasAnyAuthority('SCOPE_PROFESSIONAL', 'SCOPE_ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody UpdateFrequencyStatusRequestDTO dto) {
        try {
            Frequency updated = frequencyService.updateStatus(id, dto, currentUserId(), isAdmin());
            return ResponseEntity.ok(FrequencyDTO.fromEntity(updated));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private FrequencyStatus parseStatus(String status) {
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
