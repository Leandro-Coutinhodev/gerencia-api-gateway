package com.app.gerencia.controllers;

import com.app.gerencia.controllers.dto.CreateChargeRequestDTO;
import com.app.gerencia.controllers.dto.RegisterPaymentRequestDTO;
import com.app.gerencia.services.ChargeService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api-gateway/gerencia/financeiro/cobranca")
@PreAuthorize("hasAnyAuthority('SCOPE_SECRETARY', 'SCOPE_ADMIN')")
public class ChargeController {

    private final ChargeService chargeService;

    public ChargeController(ChargeService chargeService) {
        this.chargeService = chargeService;
    }

    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(chargeService.findAll(status));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateChargeRequestDTO req) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(chargeService.create(req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody CreateChargeRequestDTO req) {
        try {
            return ResponseEntity.ok(chargeService.update(id, req));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            chargeService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/baixa")
    public ResponseEntity<?> registerPayment(@PathVariable Long id, @RequestBody RegisterPaymentRequestDTO req) {
        try {
            return ResponseEntity.ok(chargeService.registerPayment(id, req));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/recibo")
    public ResponseEntity<?> getRecibo(@PathVariable Long id) {
        try {
            byte[] pdf = chargeService.generateReceipt(id);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(pdf);
        } catch (IllegalStateException e) {
            // SPEC_DEVIATION: design-backend.md diverge entre si (Components diz 404, Error
            // Handling Strategy diz 400 pra recibo de cobrança PENDENTE) — segui a tabela de
            // Error Handling Strategy por consistência com os demais IllegalStateException do módulo.
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
