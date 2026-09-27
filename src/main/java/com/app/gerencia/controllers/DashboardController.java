package com.app.gerencia.controllers;

import com.app.gerencia.controllers.dto.DashboardDTO;
import com.app.gerencia.services.DashboardService;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<DashboardDTO> get() {
        return ResponseEntity.ok(dashboardService.generate());
    }
}
