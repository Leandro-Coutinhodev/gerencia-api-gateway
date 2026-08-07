package com.app.gerencia.controllers;

import com.app.gerencia.controllers.dto.contract.*;
import com.app.gerencia.services.*;
import com.app.gerencia.utils.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

// ── Contract Template Controller ──────────────────────────────────────

@RestController
@RequestMapping("/api-gateway/gerencia/contract-templates")
@PreAuthorize("hasAnyAuthority('SCOPE_PROFESSIONAL', 'SCOPE_ADMIN')")
public class ContractTemplateController {

    private final ContractTemplateService templateService;

    public ContractTemplateController(ContractTemplateService templateService) {
        this.templateService = templateService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateContractTemplateRequestDTO req) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(templateService.create(req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Erro: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody CreateContractTemplateRequestDTO req) {
        try {
            return ResponseEntity.ok(templateService.update(id, req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(templateService.findAllActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(templateService.findById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deactivate(@PathVariable Long id) {
        templateService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<?> reactivate(@PathVariable Long id) {
        templateService.reactivate(id);
        return ResponseEntity.ok("Modelo reativado");
    }
}
