package com.app.gerencia.controllers;

import com.app.gerencia.controllers.dto.CreateContractRequest;
import com.app.gerencia.entities.Contract;
import com.app.gerencia.entities.ContractParticipant;
import com.app.gerencia.enums.ContractStatus;
import com.app.gerencia.repository.ContractParticipantRepository;
import com.app.gerencia.repository.ContractRepository;
import com.app.gerencia.services.ContractNotificationService;
import com.app.gerencia.services.ContractPdfService;
import com.app.gerencia.services.ContractService;
import com.app.gerencia.utils.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api-gateway/gerencia")
public class ContractController {

    private static final Logger log = LoggerFactory.getLogger(ContractController.class);

    @Autowired
    private ContractParticipantRepository participantRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private ContractService contractService;

    @Autowired
    private ContractNotificationService contractNotificationService;

    @Autowired
    private ContractPdfService contractPdfService;

//    @PostMapping("/contract")
//    public ResponseEntity<?> create(
//            @RequestBody CreateContractRequest request,
//            HttpServletRequest httpRequest
//    ) {
//        Contract contract = contractService.createContract(
//                request,
//                IpUtils.getClientIp(httpRequest)
//        );
//
//        return ResponseEntity
//                .status(HttpStatus.CREATED)
//                .body(contract.getId());
//    }

    @PostMapping(
            value = "/contract",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> create(
            @RequestPart("data") CreateContractRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file,
            HttpServletRequest httpRequest
    ) {

        Contract contract = contractService.createContract(
                request,
                file,
                IpUtils.getClientIp(httpRequest)
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(contract.getId());
    }

    @GetMapping("/contract/{token}")
    public Contract getContract(@PathVariable String token) {
        ContractParticipant participant =
                participantRepository.findByToken(token)
                        .orElseThrow(() -> new RuntimeException("Token inválido"));

        return participant.getContract();
    }

    @PostMapping("/contract/{token}/accept")
    public ResponseEntity<?> sign(
            @PathVariable String token,
            HttpServletRequest request
    ) {
        ContractParticipant participant =
                participantRepository.findByToken(token)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Token inválido"
                                )
                        );

        if (Boolean.TRUE.equals(participant.getSigned())) {
            return ResponseEntity
                    .badRequest()
                    .body("Já assinado");
        }

        Contract contract = participant.getContract();

        // Verifica ordem de assinatura
        ContractParticipant next =
                participantRepository
                        .findFirstByContractIdAndSignedFalseOrderBySigningOrderAsc(
                                contract.getId()
                        )
                        .orElseThrow();

        if (!next.getId().equals(participant.getId())) {
            return ResponseEntity
                    .badRequest()
                    .body("Ainda não é sua vez de assinar.");
        }

        // Registra a assinatura
        participant.setSigned(true);
        participant.setSignedAt(LocalDateTime.now());
        participant.setSignedIp(request.getRemoteAddr());
        participantRepository.save(participant);

        // Verifica se TODOS assinaram
        boolean allSigned = participantRepository
                .findByContractId(contract.getId())
                .stream()
                .allMatch(ContractParticipant::getSigned);

        if (allSigned) {
            contract.setStatus(ContractStatus.COMPLETED);

            // Gera o PDF final com assinaturas eletrônicas
            try {
                ContractPdfService.GeneratedPdf result =
                        contractPdfService.generate(contract);

                contract.setHash(result.getHash());
                contract.setPdfPath(result.getFilePath());

                log.info("PDF do contrato {} gerado com sucesso: {}",
                        contract.getId(), result.getFilePath());

                // Envia o PDF assinado para o e-mail do contratante
                contractNotificationService.sendSignedContractToContractor(contract);

            } catch (Exception e) {
                log.error("Erro ao gerar PDF do contrato {}: {}",
                        contract.getId(), e.getMessage(), e);
                // Não impede a assinatura — o PDF pode ser gerado depois
            }

            contractRepository.save(contract);
        }

        // Libera próximo participante
        contractNotificationService.sendNextEmail(contract.getId());

        return ResponseEntity.ok("Contrato assinado com sucesso");
    }

    /**
     * Endpoint para download do PDF assinado.
     * Disponível apenas após todas as assinaturas.
     */
    @GetMapping("/contract/{contractId}/pdf")
    public ResponseEntity<Resource> downloadPdf(@PathVariable Long contractId) {

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Contrato não encontrado"
                        )
                );

        if (contract.getPdfPath() == null || contract.getStatus() != ContractStatus.COMPLETED) {
            return ResponseEntity
                    .status(HttpStatus.PRECONDITION_FAILED)
                    .build();
        }

        File file = new File(contract.getPdfPath());
        if (!file.exists()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        Resource resource = new FileSystemResource(file);

        String fileName = String.format("contrato_%d_assinado.pdf", contract.getId());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + fileName + "\"")
                .body(resource);
    }


    @GetMapping("/contract")
    public ResponseEntity<?> getAll(){
        try{
            List<Contract> contracts = contractRepository.findAll();

            return ResponseEntity.ok(contracts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao listar");
        }
    }
}