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



    @GetMapping("/anamnesis/{anamnesisId}/referral")
    public ResponseEntity<?> getReferralByAnamnesis(@PathVariable Long anamnesisId) {
        return referralRepository.findByAnamnesisId(anamnesisId)
                .map(referral -> ResponseEntity.ok(AnamnesisReferralResponseDTO.fromEntity(referral)))
                .orElse(ResponseEntity.notFound().build());
    }


    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @PostMapping("/anamnesis/referral")
    public ResponseEntity<?> sendReferral(@RequestBody AnamnesisReferralRequestDTO request) {
        try {
            String userId = SecurityContextHolder.getContext().getAuthentication().getName();
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

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @PutMapping("/anamnesis/referral/{referralId}/assign-professional")
    public ResponseEntity<?> assignProfessional(@PathVariable Long referralId,
                                                @RequestBody AssignProfessionalRequestDTO request) {
        try {
            var updated = referralService.assignProfessional(referralId, request.professionalId());
            return ResponseEntity.ok(AnamnesisReferralResponseDTO.fromEntity(updated));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro: " + e.getMessage());
        }
    }



    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @PutMapping("/anamnesis/referral/{referralId}/assign-professional/mail")
    public ResponseEntity<?> assignProfessionalEmail(@PathVariable Long referralId,
                                                     @RequestBody AssignProfessionalRequestDTO request) {
        try {
            var updated = referralService.assignProfessionalEmail(referralId, request.professionalId());
            return ResponseEntity.ok(AnamnesisReferralResponseDTO.fromEntity(updated));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('SCOPE_PROFESSIONAL')")
    @GetMapping("/anamnesis/referral/my")
    public ResponseEntity<?> findMyReferrals() {
        try {
            String userId = SecurityContextHolder.getContext().getAuthentication().getName();
            List<AnamnesisReferral> referrals = referralService.findByProfessionalId(Long.parseLong(userId));
            var response = referrals.stream().map(AnamnesisReferralResponseDTO::fromEntity).toList();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao buscar encaminhamentos: " + e.getMessage());
        }
    }


    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @GetMapping("/anamnesis/referral/findall")
    public ResponseEntity<?> listReferral() {
        try {
            List<AnamnesisReferral> referrals = referralService.findAll();
            return ResponseEntity.ok(referrals);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    public record AssignProfessionalRequestDTO(Long professionalId) {}

    @PreAuthorize("hasAnyAuthority('SCOPE_PROFESSIONAL', 'SCOPE_ADMIN')")
    @Transactional(readOnly = true)
    @GetMapping("/anamnesis/referral/findById/{id}")
    public ResponseEntity<?> findReferralById(@PathVariable Long id) {
        try {
            AnamnesisReferral referral = referralService.findById(id);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("SCOPE_ADMIN"));

            // profissional só pode abrir o encaminhamento que é dele
            if (!isAdmin) {
                Long userId = Long.parseLong(auth.getName());
                if (referral.getProfessional() == null || !referral.getProfessional().getId().equals(userId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }

            return ResponseEntity.ok(referral);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

}
