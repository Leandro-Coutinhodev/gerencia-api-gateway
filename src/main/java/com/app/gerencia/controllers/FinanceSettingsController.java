package com.app.gerencia.controllers;

import com.app.gerencia.controllers.dto.UpdateFinanceSettingsRequestDTO;
import com.app.gerencia.services.FinanceSettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api-gateway/gerencia/financeiro/configuracao")
@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
public class FinanceSettingsController {

    private final FinanceSettingsService financeSettingsService;

    public FinanceSettingsController(FinanceSettingsService financeSettingsService) {
        this.financeSettingsService = financeSettingsService;
    }

    @GetMapping
    public ResponseEntity<?> get() {
        return ResponseEntity.ok(financeSettingsService.get());
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody UpdateFinanceSettingsRequestDTO req) {
        try {
            return ResponseEntity.ok(financeSettingsService.update(req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
