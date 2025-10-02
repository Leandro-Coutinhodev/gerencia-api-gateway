package com.app.gerencia.controllers;

import com.app.gerencia.controllers.dto.AnamnesisDTO;
import com.app.gerencia.controllers.dto.AnamnesisRequestDTO;
import com.app.gerencia.controllers.dto.AnamnesisResponseDTO;
import com.app.gerencia.entities.Anamnesis;
import com.app.gerencia.entities.Patient;
import com.app.gerencia.services.AnamnesisService;
import com.app.gerencia.services.AnamnesisTokenService;
import com.app.gerencia.services.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    public AnamnesisController(AnamnesisService anamnesisService, PatientService patientService, AnamnesisTokenService anamnesisTokenService) {
        this.anamnesisService = anamnesisService;
        this.patientService = patientService;
        this.anamnesisTokenService = anamnesisTokenService;
    }

    @PutMapping("/anamnesis/{id}/response   ")
    public ResponseEntity<Anamnesis> responder(
            @PathVariable Long id,
            @RequestPart("anamnesis") Anamnesis updatedData,
            @RequestPart(value = "report", required = false) MultipartFile report
    ) throws IOException {
        Anamnesis existing = anamnesisService.findById(id);

        if (report != null && !report.isEmpty()) {
            existing.setReport(report.getBytes());
        }

        // Atualiza os campos necessários
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

        existing.setStatus('R'); // Respondido

        return ResponseEntity.ok(anamnesisService.save(existing));
    }


    @Transactional(readOnly = true)
    @GetMapping("/anamnesis/{patientId}")
    public ResponseEntity<List<AnamnesisDTO>> findByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(
                anamnesisService.findByPatient(patientId)
                        .stream()
                        .map(AnamnesisDTO::new) // converte cada entidade em DTO
                        .toList()
        );
    }


    @PostMapping("/anamnesis")
    public ResponseEntity<AnamnesisResponseDTO> create(@RequestBody AnamnesisRequestDTO dto) {
        Patient patient = patientService.findById(dto.patientId());

        Anamnesis anamnesis = new Anamnesis();
        anamnesis.setPatient(patient);
        anamnesis.setInterviewDate(new Date());
        anamnesis.setStatus('E');

        Anamnesis saved = anamnesisService.save(anamnesis);

        // gerar token para o link
        String token = anamnesisTokenService.generateToken(patient.getId(), saved.getId());

        return ResponseEntity.ok(AnamnesisResponseDTO.fromEntity(saved, token));
    }

    @GetMapping("/anamnesis/form/{token}")
    public ResponseEntity<AnamnesisDTO> getFormData(@PathVariable String token) {
        try {
            // Decodifica o token JWT
            var jwt = anamnesisTokenService.decodeToken(token);

            Long patientId = Long.valueOf(jwt.getClaim("patientId").toString());
            Long anamnesisId = Long.valueOf(jwt.getClaim("anamnesisId").toString());

            // Busca dados no banco
            Patient patient = patientService.findById(patientId);
            Anamnesis anamnesis = anamnesisService.findById(anamnesisId);

            if (patient == null || anamnesis == null) {
                return ResponseEntity.notFound().build();
            }
            if (!anamnesis.getPatient().getId().equals(patientId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }


            // Retorna DTO completo (já mapeando dados existentes da anamnese)
            return ResponseEntity.ok(new AnamnesisDTO(anamnesis));

        } catch (Exception e) {
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


}
