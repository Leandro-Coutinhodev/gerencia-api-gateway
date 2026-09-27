package com.app.gerencia.controllers;

import com.app.gerencia.controllers.dto.record.CreateRecordRequestDTO;
import com.app.gerencia.controllers.dto.record.RecordDTO;
import com.app.gerencia.entities.AttendanceRecord;
import com.app.gerencia.services.RecordService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Fichas de atendimento preenchidas. Preenchimento é exclusivo do profissional; histórico é
// visível para profissionais e para o admin (visão total).
@RestController
@RequestMapping("/api-gateway/gerencia/records")
public class RecordController {

    private final RecordService recordService;

    public RecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    @PreAuthorize("hasAuthority('SCOPE_PROFESSIONAL')")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateRecordRequestDTO dto) {
        try {
            AttendanceRecord saved = recordService.create(dto, currentUserId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(RecordDTO.fromEntity(saved, recordService.parseAnswers(saved)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('SCOPE_PROFESSIONAL', 'SCOPE_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            AttendanceRecord record = recordService.findById(id);
            return ResponseEntity.ok(RecordDTO.fromEntity(record, recordService.parseAnswers(record)));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('SCOPE_PROFESSIONAL', 'SCOPE_ADMIN')")
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> findByPatient(@PathVariable Long patientId) {
        try {
            List<RecordDTO> dtos = recordService.findByPatient(patientId).stream()
                    .map(r -> RecordDTO.fromEntity(r, recordService.parseAnswers(r)))
                    .toList();
            return ResponseEntity.ok(dtos);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Visão total do admin sobre todas as fichas registradas
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @GetMapping
    public ResponseEntity<List<RecordDTO>> findAll() {
        List<RecordDTO> dtos = recordService.findAll().stream()
                .map(r -> RecordDTO.fromEntity(r, recordService.parseAnswers(r)))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    private Long currentUserId() {
        return Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
