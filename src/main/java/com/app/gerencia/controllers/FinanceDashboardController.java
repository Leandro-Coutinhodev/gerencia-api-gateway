package com.app.gerencia.controllers;

import com.app.gerencia.services.FinanceDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api-gateway/gerencia/financeiro/relatorio")
@PreAuthorize("hasAnyAuthority('SCOPE_SECRETARY', 'SCOPE_ADMIN')")
public class FinanceDashboardController {

    private final FinanceDashboardService financeDashboardService;

    public FinanceDashboardController(FinanceDashboardService financeDashboardService) {
        this.financeDashboardService = financeDashboardService;
    }

    @GetMapping
    public ResponseEntity<?> get(@RequestParam(required = false) String periodo) {
        try {
            return ResponseEntity.ok(financeDashboardService.generate(periodo));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
