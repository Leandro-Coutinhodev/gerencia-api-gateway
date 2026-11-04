package com.app.gerencia.controllers;

import com.app.gerencia.controllers.dto.DashboardDTO;
import com.app.gerencia.services.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api-gateway/gerencia/dashboard")
@PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_SECRETARY', 'SCOPE_PROFESSIONAL')")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<DashboardDTO> get(Authentication authentication) {
        return ResponseEntity.ok(dashboardService.generate(isAdmin(authentication)));
    }

    // Financeiro é exclusivo do ADMIN — secretária e profissional recebem o bloco null.
    private boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("SCOPE_ADMIN"));
    }
}
