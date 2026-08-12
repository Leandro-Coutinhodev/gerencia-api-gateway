package com.app.gerencia.controllers;

import com.app.gerencia.controllers.dto.contract.ContractDTO;
import com.app.gerencia.controllers.dto.contract.CreateContractForSigningRequestDTO;
import com.app.gerencia.controllers.dto.contract.CreateExternalContractRequestDTO;
import com.app.gerencia.controllers.dto.contract.SignContractRequestDTO;
import com.app.gerencia.services.ContractService;
import com.app.gerencia.utils.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;



@RestController
@RequestMapping("/api-gateway/gerencia/contracts")
public class ContractController {

    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @PreAuthorize("hasAnyAuthority('SCOPE_SECRETARY', 'SCOPE_ADMIN')")
    @PostMapping("/sign")
    public ResponseEntity<?> createForSigning(
            @RequestBody CreateContractForSigningRequestDTO req,
            HttpServletRequest httpReq
    ) {
        try {
            String userId = SecurityContextHolder.getContext().getAuthentication().getName();
            var contract = contractService.createForSigning(
                    req, IpUtils.getClientIp(httpReq), Long.parseLong(userId));
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ContractDTO.fromEntity(contract));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Erro: " + e.getMessage());
        }
    }

    //upload de contrato
    @PreAuthorize("hasAnyAuthority('SCOPE_SECRETARY', 'SCOPE_ADMIN')")
    @PostMapping(value = "/external", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createExternal(
            @RequestPart("data") CreateExternalContractRequestDTO req,
            @RequestPart("file") MultipartFile file,
            HttpServletRequest httpReq
    ) {
        try {
            String userId = SecurityContextHolder.getContext().getAuthentication().getName();
            var contract = contractService.createExternal(
                    req, file, IpUtils.getClientIp(httpReq), Long.parseLong(userId));
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ContractDTO.fromEntity(contract));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Erro: " + e.getMessage());
        }
    }

    @GetMapping("/sign/{token}")
    public ResponseEntity<?> getSigningView(@PathVariable String token) {
        try {
            return ResponseEntity.ok(contractService.getSigningView(token));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Assinar contrato (participante aceita os termos)
    @PostMapping("/sign/{token}/accept")
    public ResponseEntity<?> sign(
            @PathVariable String token,
            @RequestBody SignContractRequestDTO req,
            HttpServletRequest httpReq
    ) {
        try {
            contractService.sign(token, req, httpReq.getRemoteAddr());
            return ResponseEntity.ok("Contrato assinado com sucesso.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Erro: " + e.getMessage());
        }
    }

    // Download do PDF
//    @PreAuthorize("hasAnyAuthority('SCOPE_SECRETARY', 'SCOPE_ADMIN')")
    @GetMapping("/{contractId}/pdf")
    public ResponseEntity<byte[]> getPdf(@PathVariable Long contractId) {
        try {
            byte[] pdf = contractService.getPdf(contractId);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"contrato_" + contractId + ".pdf\"")
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }


    @PreAuthorize("hasAnyAuthority('SCOPE_SECRETARY', 'SCOPE_ADMIN')")
    @GetMapping
    public ResponseEntity<List<ContractDTO>> getAll() {
        return ResponseEntity.ok(contractService.findAll());
    }


    @PreAuthorize("hasAnyAuthority('SCOPE_SECRETARY', 'SCOPE_ADMIN')")
    @GetMapping("/bypatient/{patientId}")
    public ResponseEntity<List<ContractDTO>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(contractService.findByPatient(patientId));
    }
}

