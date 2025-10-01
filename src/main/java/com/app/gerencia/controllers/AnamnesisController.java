package com.app.gerencia.controllers;

import com.app.gerencia.controllers.dto.AnamnesisDTO;
import com.app.gerencia.controllers.dto.AnamnesisRequestDTO;
import com.app.gerencia.controllers.dto.AnamnesisResponseDTO;
import com.app.gerencia.entities.Anamnesis;
import com.app.gerencia.entities.Patient;
import com.app.gerencia.services.AnamnesisService;
import com.app.gerencia.services.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    public AnamnesisController(AnamnesisService anamnesisService, PatientService patientService) {
        this.anamnesisService = anamnesisService;
        this.patientService = patientService;
    }

    @PostMapping("/anamnesis/{patientId}")
    public ResponseEntity<Anamnesis> create(
            @PathVariable Long patientId,
            @RequestPart("anamnesis") Anamnesis anamnesis,
            @RequestPart(value = "report", required = false) MultipartFile report
    ) throws IOException {
        if (report != null && !report.isEmpty()) {
            anamnesis.setReport(report.getBytes());
        }
        return ResponseEntity.ok(anamnesisService.save(anamnesis, patientId));
    }

    @GetMapping("/anamnesis/{patientId}")
    public ResponseEntity<List<Anamnesis>> findByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(anamnesisService.findByPatient(patientId));
    }

    @PostMapping("/anamnesis")
    public ResponseEntity<AnamnesisResponseDTO> create(@RequestBody AnamnesisRequestDTO dto) {
        Patient patient = patientService.findById(dto.patientId());

        Anamnesis anamnesis = new Anamnesis();
        anamnesis.setPatient(patient);
        anamnesis.setInterviewDate(new Date());
        anamnesis.setStatus('E'); // pode até remover se já está default no entity

        Anamnesis saved = anamnesisService.save(anamnesis);

        return ResponseEntity.ok(AnamnesisResponseDTO.fromEntity(saved));
    }


    @GetMapping("/anamnesis")
    public ResponseEntity<List<AnamnesisResponseDTO>> findAll() {
        List<Anamnesis> anamneses = anamnesisService.findAll();

        List<AnamnesisResponseDTO> dtos = anamneses.stream()
                .map(AnamnesisResponseDTO::fromEntity)   // <-- aqui
                .collect(Collectors.toList());

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

}
