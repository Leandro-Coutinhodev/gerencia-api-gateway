package com.app.gerencia.controllers;

import com.app.gerencia.entities.Guardian;
import com.app.gerencia.entities.Patient;
import com.app.gerencia.repository.GuardianRepository;
import com.app.gerencia.services.PatientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("api-gateway/gerencia")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private GuardianRepository guardianRepository;

    @PostMapping("/patient")
    public ResponseEntity<String> save(@RequestPart("patient") Patient patient, @RequestPart("photo") MultipartFile photo){

        try{
            if(photo != null && !photo.isEmpty())
                patient.setPhoto(photo.getBytes());

            String response = patientService.save(patient);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Erro ao salvar");
        }
    }

    @GetMapping("/patient/{id}")
    public ResponseEntity<Patient> findById (@PathVariable Long id){

        try{
            Patient patient = patientService.findById(id);

            return ResponseEntity.ok(patient);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/patient")
    public ResponseEntity<List<Patient>> findALl(){

        try{
            List<Patient> patients = patientService.findAll();

            return ResponseEntity.ok(patients);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping(value = "/patient/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> update(
            @PathVariable Long id,
            @RequestPart("patient") String patientJson,
            @RequestPart(value = "photo", required = false) MultipartFile photo
    ) {
        try {
            // Desserializar JSON
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule()); // Para LocalDate/LocalDateTime
            Patient patient = mapper.readValue(patientJson, Patient.class);

            // Processar foto se vier uma nova
            if (photo != null && !photo.isEmpty()) {
                patient.setPhoto(photo.getBytes());
            }
            // Se não vier foto, o service deve manter a foto existente

            String response = patientService.update(patient, id);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Erro ao processar dados: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao atualizar: " + e.getMessage());
        }
    }

    @GetMapping("/patient/search/cpf")
    public ResponseEntity<List<Patient>> findByCpf(@RequestParam(required = false) String cpf) {
        try {
            List<Patient> patients = patientService.findByCpf(cpf);
            return ResponseEntity.ok(patients);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/patient/search/name")
    public ResponseEntity<List<Patient>> searchByName(@RequestParam String nome) {
        try {
            List<Patient> patients = patientService.findByName(nome);
            return ResponseEntity.ok(patients);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/patient/guardian/{guardianId}")
    public ResponseEntity<List<Patient>> searchByGuardian(@PathVariable Long guardianId) {
        try {
            List<Patient> patients = patientService.searchByGuardian(guardianId);
            return ResponseEntity.ok(patients);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/patient/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id){

        try{
            String response = patientService.delete(id);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao excluir!");
        }
    }

    @GetMapping("/patient/search")
    public ResponseEntity<List<Patient>> searchPatients(@RequestParam String query) {
        try {
            List<Patient> patients = patientService.searchByNameOrCpf(query);
            return ResponseEntity.ok(patients);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
