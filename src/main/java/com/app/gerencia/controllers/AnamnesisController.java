package com.app.gerencia.controllers;

import com.app.gerencia.controllers.dto.*;
import com.app.gerencia.entities.*;
import com.app.gerencia.repository.AnamnesisReferralRepository;
import com.app.gerencia.services.*;
import jakarta.persistence.EntityNotFoundException;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

//@RestController
//@RequestMapping("/api-gateway/gerencia")
//public class AnamnesisController {
//
//    private final AnamnesisService anamnesisService;
//    private final PatientService patientService;
//    private final AnamnesisTokenService anamnesisTokenService;
//    private final AnamnesisReferralService referralService;
//    private final AnamnesisReferralRepository referralRepository;
//
//    @Value("${link.host}")
//    private String host;
//
//    public AnamnesisController(AnamnesisService anamnesisService,
//                               PatientService patientService,
//                               AnamnesisTokenService anamnesisTokenService,
//                               AnamnesisReferralService referralService,
//                               AnamnesisReferralRepository referralRepository) {
//        this.anamnesisService = anamnesisService;
//        this.patientService = patientService;
//        this.anamnesisTokenService = anamnesisTokenService;
//        this.referralService = referralService;
//        this.referralRepository = referralRepository;
//    }

@RestController
@RequestMapping("/api-gateway/gerencia")
public class AnamnesisController {

    private final AnamnesisService anamnesisService;
    private final AnamnesisTemplateService templateService;
    private final AnamnesisAnswerService answerService;
    private final PatientService patientService;
    private final AnamnesisTokenService anamnesisTokenService;
    private final AnamnesisReferralService referralService;
    private final AnamnesisReferralRepository referralRepository;

    @Value("${link.host}")
    private String host;

    public AnamnesisController(AnamnesisService anamnesisService,
                               AnamnesisTemplateService templateService,
                               AnamnesisAnswerService answerService,
                               PatientService patientService,
                               AnamnesisTokenService anamnesisTokenService,
                               AnamnesisReferralService referralService,
                               AnamnesisReferralRepository referralRepository) {
        this.anamnesisService = anamnesisService;
        this.templateService = templateService;
        this.answerService = answerService;
        this.patientService = patientService;
        this.anamnesisTokenService = anamnesisTokenService;
        this.referralService = referralService;
        this.referralRepository = referralRepository;
    }

    // =========================================================
    // TEMPLATES
    // =========================================================

    @PostMapping("/anamnesis/templates")
    public ResponseEntity<AnamnesisTemplateDTO> createTemplate(
            @RequestBody AnamnesisTemplateRequestDTO dto) {
        AnamnesisTemplate saved = templateService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AnamnesisTemplateDTO.fromEntity(saved));
    }

    @PutMapping("/anamnesis/templates/{id}")
    public ResponseEntity<AnamnesisTemplateDTO> updateTemplate(
            @PathVariable Long id,
            @RequestBody AnamnesisTemplateRequestDTO dto) {
        AnamnesisTemplate updated = templateService.update(id, dto);
        return ResponseEntity.ok(AnamnesisTemplateDTO.fromEntity(updated));
    }

    @GetMapping("/anamnesis/templates")
    public ResponseEntity<List<AnamnesisTemplateDTO>> listTemplates() {
        List<AnamnesisTemplateDTO> templates = templateService.findAllActive()
                .stream()
                .map(AnamnesisTemplateDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/anamnesis/templates/{id}")
    public ResponseEntity<AnamnesisTemplateDTO> getTemplate(@PathVariable Long id) {
        try {
            AnamnesisTemplate template = templateService.findById(id);
            return ResponseEntity.ok(AnamnesisTemplateDTO.fromEntity(template));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/anamnesis/templates/{id}")
    public ResponseEntity<Void> deactivateTemplate(@PathVariable Long id) {
        try {
            templateService.deactivate(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/anamnesis/templates/{id}/reactivate")
    public ResponseEntity<AnamnesisTemplateDTO> reactivateTemplate(@PathVariable Long id) {
        try {
            AnamnesisTemplate template = templateService.reactivate(id);
            return ResponseEntity.ok(AnamnesisTemplateDTO.fromEntity(template));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // =========================================================
    // ANAMNESE — CRUD
    // =========================================================

    @PostMapping("/anamnesis")
    public ResponseEntity<Void> create(@RequestBody AnamnesisRequestDTO dto) {
        try {
            Patient patient = patientService.findById(dto.patientId());
            AnamnesisTemplate template = templateService.findById(dto.templateId());

            Anamnesis anamnesis = new Anamnesis();
            anamnesis.setPatient(patient);
            anamnesis.setTemplate(template);
            anamnesis.setInterviewDate(new Date());
            anamnesis.setStatus('E');

            Anamnesis saved = anamnesisService.save(anamnesis);
            anamnesisTokenService.generateToken(patient.getId(), saved.getId());

            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/anamnesis")
    public ResponseEntity<List<AnamnesisResponseDTO>> findAll() {
        List<AnamnesisResponseDTO> dtos = anamnesisService.findAll()
                .stream()
                .map(a -> {
                    String token = anamnesisTokenService.generateToken(
                            a.getPatient().getId(),
                            a.getId()
                    );
                    return AnamnesisResponseDTO.fromEntity(a, token, host);
                })
                .toList();
        System.out.println(dtos);
        return ResponseEntity.ok(dtos);
    }

    @Transactional(readOnly = true)
    @GetMapping("/anamnesis/{id}")
    public ResponseEntity<AnamnesisDTO> findById(@PathVariable Long id) {
        try {
            Anamnesis anamnesis = anamnesisService.findById(id);
            return ResponseEntity.ok(new AnamnesisDTO(anamnesis));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Transactional(readOnly = true)
    @GetMapping("/anamnesis/bypatient/{patientId}")
    public ResponseEntity<List<AnamnesisDTO>> findByPatient(@PathVariable Long patientId) {
        try {
            Patient patient = patientService.findById(patientId);
            List<AnamnesisDTO> dtos = anamnesisService.findByPatient(patientId)
                    .stream()
                    .map(anamnesis -> {
                        String token = anamnesisTokenService.generateToken(
                                patient.getId(),
                                anamnesis.getId()
                        );
                        return new AnamnesisDTO(anamnesis, token, host);
                    })
                    .toList();
            return ResponseEntity.ok(dtos);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/anamnesis/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        try {
            anamnesisService.delete(id);
            return ResponseEntity.ok("Deletado com sucesso");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao deletar");
        }
    }

    // =========================================================
    // ANAMNESE — FORMULÁRIO E LINK
    // =========================================================

    @PostMapping("/anamnesis/link")
    public ResponseEntity<String> generateLink(@RequestBody AnamnesisRequestDTO dto) {
        try {
            Patient patient = patientService.findById(dto.patientId());
            AnamnesisTemplate template = templateService.findById(dto.templateId());

            Anamnesis anamnesis = new Anamnesis();
            anamnesis.setPatient(patient);
            anamnesis.setTemplate(template);
            anamnesis.setInterviewDate(new Date());
            anamnesis.setStatus('E');

            Anamnesis saved = anamnesisService.save(anamnesis);
            String token = anamnesisTokenService.generateToken(patient.getId(), saved.getId());
            String link = host + "/formulario?token=" + token;

            return ResponseEntity.ok(link);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/anamnesis/form/{token}")
    public ResponseEntity<AnamnesisFormDTO> getFormData(@PathVariable String token) {
        try {
            Long patientId = anamnesisTokenService.getPatientId(token);
            Long anamnesisId = anamnesisTokenService.getAnamnesisId(token);

            Patient patient = patientService.findById(patientId);
            Anamnesis anamnesis = anamnesisService.findById(anamnesisId);

            if (!anamnesis.getPatient().getId().equals(patientId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Busca as respostas já preenchidas (caso o paciente volte ao link)
            List<AnamnesisAnswer> existingAnswers = answerService.findByAnamnesisId(anamnesisId);

            return ResponseEntity.ok(new AnamnesisFormDTO(anamnesis, existingAnswers, token, host));

        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // =========================================================
    // ANAMNESE — RESPOSTA DO PACIENTE
    // =========================================================

    @PutMapping("/anamnesis/{id}/response")
    public ResponseEntity<AnamnesisDTO> response(
            @PathVariable Long id,
            @RequestPart("answers") AnamnesisResponseSubmitDTO dto,
            MultipartHttpServletRequest multipartRequest  // ← substitui MultipartFile[] files
    ) {
        try {
            // Extrai apenas os parts cujo nome segue a convenção "file_{fieldId}"
            List<MultipartFile> files = multipartRequest.getFileMap()
                    .entrySet().stream()
                    .filter(e -> e.getKey().startsWith("file_"))
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());

            Anamnesis saved = anamnesisService.respond(id, dto, files);
            return ResponseEntity.ok(new AnamnesisDTO(saved));

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // =========================================================
    // ANAMNESE — ARQUIVO PDF DE UM CAMPO
    // =========================================================

    @GetMapping("/anamnesis/{id}/field/{fieldId}/file")
    public ResponseEntity<byte[]> viewFieldFile(
            @PathVariable Long id,
            @PathVariable Long fieldId) {

        return answerService.findFileAnswer(id, fieldId)
                .map(answer -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "inline; filename=" + answer.getFileName())
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(answer.getFileData()))
                .orElse(ResponseEntity.notFound().build());
    }



//    @PreAuthorize("hasAnyAuthority('SCOPE_PROFESSIONAL', 'SCOPE_ADMIN')")
//    @PostMapping("/anamnesis/referral")
//    public ResponseEntity<?> sendReferral(@RequestBody AnamnesisReferralRequestDTO request) {
//
//        try {
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//            if (authentication == null || !authentication.isAuthenticated()) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado");
//            }
//
//            String userId = authentication.getName();
//
//            System.out.println("User ID from authentication: " + userId);
//
//            // Validações
//            if (userId == null || userId.trim().isEmpty()) {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                        .body("ID do usuário não encontrado no token");
//            }
//
//            // Converte para Long
//            Long userIdLong = Long.parseLong(userId);
//
//            System.out.println("Calling referralService.createReferral with userId: " + userIdLong);
//
//            var referral = referralService.createReferral(userIdLong, request);
//
//            System.out.println("Referral created successfully: " + referral);
//
//            return ResponseEntity.ok(referral);
//
//        } catch (NumberFormatException e) {
//            System.err.println("NumberFormatException: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body("ID do usuário inválido no token: " + e.getMessage());
//        } catch (Exception e) {
//            System.err.println("Error in sendReferral: " + e.getMessage());
//            e.printStackTrace(); // Isso vai mostrar a stack trace completa no console
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Erro interno: " + e.getMessage());
//        }
//    }
//
//    @PreAuthorize("hasAnyAuthority('SCOPE_PROFESSIONAL', 'SCOPE_ADMIN')")
//    @PutMapping("/anamnesis/referral/{referralId}/assign-assistant")
//    public ResponseEntity<?> assignAssistantToReferral(
//            @PathVariable Long referralId,
//            @RequestBody AssignAssistantRequestDTO request) {
//        try {
//            var updatedReferral = referralService.assignAssistant(referralId, request.assistantId());
//            var dto = AnamnesisReferralResponseDTO.fromEntity(updatedReferral);
//            return ResponseEntity.ok(dto);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Erro ao atribuir assistente: " + e.getMessage());
//        }
//    }
//    @PreAuthorize("hasAnyAuthority('SCOPE_PROFESSIONAL', 'SCOPE_ADMIN')")
//    @PutMapping("/anamnesis/referral/{referralId}/assign-assistant/mail")
//    public ResponseEntity<?> assignAssistantToReferralEmail(
//            @PathVariable Long referralId,
//            @RequestBody AssignAssistantRequestDTO request) {
//        try {
//            var updatedReferral = referralService.assignAssistantEmail(referralId, request.assistantId());
//            var dto = AnamnesisReferralResponseDTO.fromEntity(updatedReferral);
//            return ResponseEntity.ok(dto);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Erro ao atribuir assistente: " + e.getMessage());
//        }
//    }
//
//
//    // DTO para a requisição de atribuição
//    public record AssignAssistantRequestDTO(Long assistantId) {}
//
//    @GetMapping("/anamnesis/{anamnesisId}/referral")
//    public ResponseEntity<?> getReferralByAnamnesis(@PathVariable Long anamnesisId) {
//        return referralRepository.findByAnamnesisId(anamnesisId)
//                .map(referral -> ResponseEntity.ok(AnamnesisReferralResponseDTO.fromEntity(referral)))
//                .orElse(ResponseEntity.notFound().build());
//    }
//
    @GetMapping("/anamnesis/referral/findByAssistant/{assistantId}")
    public ResponseEntity<?> findAllReferral(@PathVariable Long assistantId) {
        try {
            List<AnamnesisReferral> referrals = referralService.findByAssistantId(assistantId);

            List<AnamnesisReferralResponseDTO> response = referrals.stream()
                    .map(AnamnesisReferralResponseDTO::fromEntity)
                    .toList();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao buscar encaminhamentos: " + e.getMessage());
        }
    }
//
//    @GetMapping("/anamnesis/referral/{patientId}")
//    public ResponseEntity<?> historySend(@PathVariable Long patientId){
//        try {
//
//            List<Anamnesis> anamneses = anamnesisService.findByPatient(patientId);
//
//            if (anamneses.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                        .body("Nenhuma anamnese encontrada para o paciente de ID: " + patientId);
//            }
//
//
//            List<Long> anamnesisIds = anamneses.stream()
//                    .map(Anamnesis::getId)
//                    .collect(Collectors.toList());
//
//
//            List<AnamnesisReferral> referrals = referralRepository.findByAnamnesisIdIn(anamnesisIds);
//
//            if (referrals.isEmpty()) {
//                return ResponseEntity.ok(Collections.emptyList());
//            }
//
//
//            List<Map<String, Object>> response = referrals.stream().map(ref -> {
//                Map<String, Object> map = new HashMap<>();
//                map.put("referralId", ref.getId());
//                map.put("sentAt", ref.getSentAt());
//                map.put("patientName", ref.getAnamnesis().getPatient().getName());
//                map.put("guardianName", ref.getAnamnesis().getPatient().getGuardian().getName());
//                map.put("assistantName",
//                        ref.getAssistant() != null ? ref.getAssistant().getName() : "Ainda não vinculado");
//                map.put("senderName",
//                        ref.getSender() != null ? ref.getSender().getName() : "Desconhecido");
//                map.put("anamnesisId", ref.getAnamnesis().getId());
//                return map;
//            }).collect(Collectors.toList());
//
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Erro ao buscar histórico de encaminhamentos: " + e.getMessage());
//        }
//
//    }
//
//    @GetMapping("/anamnesis/referral/findById/{id}")
//    public ResponseEntity<?> findReferralById(@PathVariable Long id){
//        try{
//            AnamnesisReferral ar = referralService.findById(id);
//            return ResponseEntity.ok(ar);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().build();
//        }
//    }
//
    @GetMapping("/anamnesis/referral/findall")
    public ResponseEntity<?> listReferral(){
        try{
            List<AnamnesisReferral> referrals = referralService.findAll();
            return ResponseEntity.ok(referrals);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PreAuthorize("hasAnyAuthority('SCOPE_PROFESSIONAL', 'SCOPE_ADMIN')")
    @PostMapping("/anamnesis/referral")
    public ResponseEntity<?> sendReferral(@RequestBody AnamnesisReferralRequestDTO request) {
        try {
            String userId = SecurityContextHolder.getContext()
                    .getAuthentication().getName();
            var referral = referralService.createReferral(Long.parseLong(userId), request);
            return ResponseEntity.ok(AnamnesisReferralResponseDTO.fromEntity(referral));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('SCOPE_PROFESSIONAL', 'SCOPE_ADMIN')")
    @PutMapping("/anamnesis/referral/{referralId}/assign-assistant")
    public ResponseEntity<?> assignAssistant(@PathVariable Long referralId,
                                             @RequestBody AssignAssistantRequestDTO request) {
        try {
            var updated = referralService.assignAssistant(referralId, request.assistantId());
            return ResponseEntity.ok(AnamnesisReferralResponseDTO.fromEntity(updated));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('SCOPE_PROFESSIONAL', 'SCOPE_ADMIN')")
    @PutMapping("/anamnesis/referral/{referralId}/assign-assistant/mail")
    public ResponseEntity<?> assignAssistantEmail(@PathVariable Long referralId,
                                                  @RequestBody AssignAssistantRequestDTO request) {
        try {
            var updated = referralService.assignAssistantEmail(referralId, request.assistantId());
            return ResponseEntity.ok(AnamnesisReferralResponseDTO.fromEntity(updated));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro: " + e.getMessage());
        }
    }

    public record AssignAssistantRequestDTO(Long assistantId) {}


}
