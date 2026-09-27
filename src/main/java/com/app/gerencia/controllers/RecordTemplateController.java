package com.app.gerencia.controllers;

import com.app.gerencia.controllers.dto.record.RecordTemplateDTO;
import com.app.gerencia.controllers.dto.record.RecordTemplateRequestDTO;
import com.app.gerencia.entities.RecordTemplate;
import com.app.gerencia.services.RecordTemplateService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Modelos de ficha: cada profissional monta seus próprios modelos combinando atividades do seu
// banco pessoal (com campos de execução) e campos gerais. Admin tem visão total (leitura).
@RestController
@RequestMapping("/api-gateway/gerencia/record-templates")
public class RecordTemplateController {

    private final RecordTemplateService templateService;

    public RecordTemplateController(RecordTemplateService templateService) {
        this.templateService = templateService;
    }

    @PreAuthorize("hasAuthority('SCOPE_PROFESSIONAL')")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody RecordTemplateRequestDTO dto) {
        try {
            RecordTemplate saved = templateService.create(dto, currentUserId());
            return ResponseEntity.status(HttpStatus.CREATED).body(RecordTemplateDTO.fromEntity(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasAuthority('SCOPE_PROFESSIONAL')")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody RecordTemplateRequestDTO dto) {
        try {
            RecordTemplate updated = templateService.update(id, dto, currentUserId(), isAdmin());
            return ResponseEntity.ok(RecordTemplateDTO.fromEntity(updated));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('SCOPE_PROFESSIONAL', 'SCOPE_ADMIN')")
    @GetMapping
    public ResponseEntity<List<RecordTemplateDTO>> list() {
        List<RecordTemplateDTO> dtos = templateService.findVisible(currentUserId(), isAdmin())
                .stream().map(RecordTemplateDTO::fromEntity).toList();
        return ResponseEntity.ok(dtos);
    }

    // Modelos ativos do próprio profissional — usado no seletor de "Nova Ficha"
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('SCOPE_PROFESSIONAL')")
    @GetMapping("/active")
    public ResponseEntity<List<RecordTemplateDTO>> listActiveMine() {
        List<RecordTemplateDTO> dtos = templateService.findActiveByProfessional(currentUserId())
                .stream().map(RecordTemplateDTO::fromEntity).toList();
        return ResponseEntity.ok(dtos);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('SCOPE_PROFESSIONAL', 'SCOPE_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(RecordTemplateDTO.fromEntity(templateService.findById(id)));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Soft delete: mantém histórico de fichas já preenchidas com este modelo
    @PreAuthorize("hasAuthority('SCOPE_PROFESSIONAL')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        try {
            templateService.setActive(id, false, currentUserId(), isAdmin());
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PreAuthorize("hasAuthority('SCOPE_PROFESSIONAL')")
    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<?> reactivate(@PathVariable Long id) {
        try {
            RecordTemplate updated = templateService.setActive(id, true, currentUserId(), isAdmin());
            return ResponseEntity.ok(RecordTemplateDTO.fromEntity(updated));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
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
