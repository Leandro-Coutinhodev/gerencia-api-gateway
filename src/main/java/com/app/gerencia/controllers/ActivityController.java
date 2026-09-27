package com.app.gerencia.controllers;

import com.app.gerencia.controllers.dto.record.ActivityDTO;
import com.app.gerencia.controllers.dto.record.ActivityRequestDTO;
import com.app.gerencia.entities.Activity;
import com.app.gerencia.services.ActivityService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Banco de atividades: cada profissional cadastra e gerencia suas próprias atividades reutilizáveis.
// O admin tem apenas visão total (leitura) sobre as atividades de todos os profissionais.
@RestController
@RequestMapping("/api-gateway/gerencia/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @PreAuthorize("hasAuthority('SCOPE_PROFESSIONAL')")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody ActivityRequestDTO dto) {
        try {
            Activity saved = activityService.create(dto, currentUserId());
            return ResponseEntity.status(HttpStatus.CREATED).body(ActivityDTO.fromEntity(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasAnyAuthority('SCOPE_PROFESSIONAL', 'SCOPE_ADMIN')")
    @GetMapping
    public ResponseEntity<List<ActivityDTO>> list() {
        List<ActivityDTO> dtos = activityService.findVisible(currentUserId(), isAdmin())
                .stream().map(ActivityDTO::fromEntity).toList();
        return ResponseEntity.ok(dtos);
    }

    @PreAuthorize("hasAnyAuthority('SCOPE_PROFESSIONAL', 'SCOPE_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ActivityDTO.fromEntity(activityService.findById(id)));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasAuthority('SCOPE_PROFESSIONAL')")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ActivityRequestDTO dto) {
        try {
            Activity updated = activityService.update(id, dto, currentUserId(), isAdmin());
            return ResponseEntity.ok(ActivityDTO.fromEntity(updated));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('SCOPE_PROFESSIONAL')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> setStatus(@PathVariable Long id, @RequestBody StatusRequestDTO body) {
        try {
            Activity updated = activityService.setActive(id, body.active(), currentUserId(), isAdmin());
            return ResponseEntity.ok(ActivityDTO.fromEntity(updated));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    public record StatusRequestDTO(boolean active) {}

    private Long currentUserId() {
        return Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("SCOPE_ADMIN"));
    }
}
