package com.app.gerencia.controllers;

import com.app.gerencia.controllers.dto.AnamnesisDTO;
import com.app.gerencia.controllers.dto.AnamnesisReferralRequestDTO;
import com.app.gerencia.controllers.dto.AnamnesisRequestDTO;
import com.app.gerencia.controllers.dto.AnamnesisResponseDTO;
import com.app.gerencia.entities.Anamnesis;
import com.app.gerencia.entities.Patient;
import com.app.gerencia.services.AnamnesisReferralService;
import com.app.gerencia.services.AnamnesisService;
import com.app.gerencia.services.AnamnesisTokenService;
import com.app.gerencia.services.PatientService;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api-gateway/gerencia")
public class AnamnesisController {

    private final AnamnesisService anamnesisService;
    private final PatientService patientService;
    private final AnamnesisTokenService anamnesisTokenService;
    private final AnamnesisReferralService referralService;

    public AnamnesisController(AnamnesisService anamnesisService,
                               PatientService patientService,
                               AnamnesisTokenService anamnesisTokenService,
                               AnamnesisReferralService referralService) {
        this.anamnesisService = anamnesisService;
        this.patientService = patientService;
        this.anamnesisTokenService = anamnesisTokenService;
        this.referralService = referralService;
    }


    @PutMapping("/anamnesis/{id}/response")
    public ResponseEntity<AnamnesisDTO> response(
            @PathVariable Long id,
            @RequestPart("anamnesis") Anamnesis updatedData,
            @RequestPart(value = "reports", required = false) MultipartFile[] reports
    ) throws IOException {
        Anamnesis existing = anamnesisService.findById(id);

        // 🔹 Se múltiplos arquivos foram enviados → faz merge
        if (reports != null && reports.length > 0) {
            byte[] mergedPdf = mergePdfFiles(reports);
            existing.setReport(mergedPdf);
        }

        // 🔹 Atualiza campos do formulário
        existing.setDiagnoses(updatedData.getDiagnoses());
        existing.setMedicationAndAllergies(updatedData.getMedicationAndAllergies());
        existing.setIndications(updatedData.getIndications());
        existing.setObjectives(updatedData.getObjectives());
        existing.setDevelopmentHistory(updatedData.getDevelopmentHistory());
        existing.setPreferences(updatedData.getPreferences());
        existing.setInterferingBehaviors(updatedData.getInterferingBehaviors());
        existing.setQualityOfLife(updatedData.getQualityOfLife());
        existing.setFeeding(updatedData.getFeeding());
        existing.setSleep(updatedData.getSleep());
        existing.setTherapists(updatedData.getTherapists());

        existing.setStatus('A'); // Respondido

        Anamnesis saved = anamnesisService.save(existing);
        return ResponseEntity.ok(new AnamnesisDTO(saved));
    }

    /**
     * Faz o merge de múltiplos PDFs em um único arquivo.
     */

    private byte[] mergePdfFiles(MultipartFile[] files) throws IOException {
        PDFMergerUtility merger = new PDFMergerUtility();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        merger.setDestinationStream(outputStream);

        for (MultipartFile file : files) {
            merger.addSource(file.getInputStream());
        }

        merger.mergeDocuments(null);
        return outputStream.toByteArray();
    }





    @Transactional(readOnly = true)
    @GetMapping("/anamnesis/bypatient/{patientId}")
    public ResponseEntity<List<AnamnesisDTO>> findByPatient(@PathVariable Long patientId) {
        // Verifica se o paciente existe
        Patient patient = patientService.findById(patientId);

        return ResponseEntity.ok(
                anamnesisService.findByPatient(patientId)
                        .stream()
                        .map(anamnesis -> {
                            // Gera o token para cada anamnese, igual no método create
                            String token = anamnesisTokenService.generateToken(patient.getId(), anamnesis.getId());
                            return new AnamnesisDTO(anamnesis, token);
                        })
                        .toList()
        );
    }


    @PostMapping("/anamnesis")
    public ResponseEntity<Void> create(@RequestBody AnamnesisRequestDTO dto) {
        Patient patient = patientService.findById(dto.patientId());

        Anamnesis anamnesis = new Anamnesis();
        anamnesis.setPatient(patient);
        anamnesis.setInterviewDate(new Date());
        anamnesis.setStatus('E');

        Anamnesis saved = anamnesisService.save(anamnesis);

        // gerar token para o link
        anamnesisTokenService.generateToken(patient.getId(), saved.getId());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/anamnesis/form/{token}")
    public ResponseEntity<AnamnesisDTO> getFormData(@PathVariable String token) {
        try {
            var jwt = anamnesisTokenService.decodeToken(token);

            // 🔑 Recupera os claims como Object e converte para Long
            Object patientClaim = jwt.getClaims().get("patientId");
            Long patientId = patientClaim instanceof Number
                    ? ((Number) patientClaim).longValue()
                    : Long.valueOf(patientClaim.toString());

            Object anamnesisClaim = jwt.getClaims().get("anamnesisId");
            Long anamnesisId = anamnesisClaim instanceof Number
                    ? ((Number) anamnesisClaim).longValue()
                    : Long.valueOf(anamnesisClaim.toString());

            // Busca dados no banco
            Patient patient = patientService.findById(patientId);
            Anamnesis anamnesis = anamnesisService.findById(anamnesisId);

            if (patient == null || anamnesis == null) {
                return ResponseEntity.notFound().build();
            }
            if (!anamnesis.getPatient().getId().equals(patientId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Retorna DTO com o token incluso no link
            return ResponseEntity.ok(new AnamnesisDTO(anamnesis, token));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }




    @GetMapping("/anamnesis")
    public ResponseEntity<List<AnamnesisResponseDTO>> findAll() {
        List<Anamnesis> anamneses = anamnesisService.findAll();

        List<AnamnesisResponseDTO> dtos = anamneses.stream()
                .map(a -> {
                    // gera o token a cada requisição (determinístico, se você não incluir jti/issuedAt)
                    String token = anamnesisTokenService.generateToken(
                            a.getPatient().getId(),
                            a.getId()
                    );
                    return AnamnesisResponseDTO.fromEntity(a, token);
                })
                .toList();

        return ResponseEntity.ok(dtos);
    }


    @DeleteMapping("/anamnesis/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id){
        try {

            String response = anamnesisService.delete(id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Erro ao deletar", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/anamnesis/link")
    public ResponseEntity<String> generateLink(@RequestBody AnamnesisRequestDTO dto) {
        Patient patient = patientService.findById(dto.patientId());

        Anamnesis anamnesis = new Anamnesis();
        anamnesis.setPatient(patient);
        anamnesis.setStatus('E');

        Anamnesis saved = anamnesisService.save(anamnesis, dto.patientId());

        String token = anamnesisTokenService.generateToken(patient.getId(), saved.getId());

        String link = "http://localhost:3000/formulario?token=" + token;

        return ResponseEntity.ok(link);
    }

    @Transactional(readOnly = true)
    @GetMapping("/anamnesis/{id}")
    public ResponseEntity<AnamnesisDTO> findById(@PathVariable Long id) {
        try {
            Anamnesis anamnesis = anamnesisService.findById(id);


            AnamnesisDTO anamnesisDTO = new AnamnesisDTO(anamnesis);

            return ResponseEntity.ok(anamnesisDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/anamnesis/{id}/report")
    public ResponseEntity<byte[]> viewReport(@PathVariable Long id) {
        Anamnesis anamnesis = anamnesisService.findById(id);

        if (anamnesis == null || anamnesis.getReport() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=laudo_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(anamnesis.getReport());
    }

    @PreAuthorize("hasAuthority('SCOPE_PROFESSIONAL')")
    @PostMapping("/anamnesis/referral")
    public ResponseEntity<?> sendReferral(@RequestBody AnamnesisReferralRequestDTO request) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado");
            }

            String userId = authentication.getName();

            System.out.println("User ID from authentication: " + userId);

            // Validações
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("ID do usuário não encontrado no token");
            }

            // Converte para Long
            Long userIdLong = Long.parseLong(userId);

            System.out.println("Calling referralService.createReferral with userId: " + userIdLong);

            var referral = referralService.createReferral(userIdLong, request);

            System.out.println("Referral created successfully: " + referral);

            return ResponseEntity.ok(referral);

        } catch (NumberFormatException e) {
            System.err.println("NumberFormatException: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("ID do usuário inválido no token: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error in sendReferral: " + e.getMessage());
            e.printStackTrace(); // Isso vai mostrar a stack trace completa no console
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('SCOPE_PROFESSIONAL')")
    @PutMapping("/anamnesis/referral/{referralId}/assign-assistant")
    public ResponseEntity<?> assignAssistantToReferral(
            @PathVariable Long referralId,
            @RequestBody AssignAssistantRequestDTO request) {

        try {
            var updatedReferral = referralService.assignAssistant(referralId, request.assistantId());
            return ResponseEntity.ok(updatedReferral);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao atribuir assistente: " + e.getMessage());
        }
    }

    // DTO para a requisição de atribuição
    public record AssignAssistantRequestDTO(Long assistantId) {}

}
