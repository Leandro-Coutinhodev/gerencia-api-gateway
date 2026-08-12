package com.app.gerencia.controllers;

import com.app.gerencia.controllers.dto.RecoveryRequest;
import com.app.gerencia.controllers.dto.ResetPasswordRequest;
import com.app.gerencia.services.PasswordRecoveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api-gateway/gerencia/recovery")
public class PasswordRecoveryController {

    private final PasswordRecoveryService service;

    public PasswordRecoveryController(PasswordRecoveryService service) {
        this.service = service;
    }


    @PostMapping
    public ResponseEntity<Void> recovery(@RequestBody RecoveryRequest request) {
        service.requestRecovery(request.email());
        return ResponseEntity.ok().build();
    }


    @PostMapping("/reset")
    public ResponseEntity<Void> reset(@RequestBody ResetPasswordRequest request) {
        service.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok().build();
    }
}