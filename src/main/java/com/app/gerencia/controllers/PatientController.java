package com.app.gerencia.controllers;

import com.app.gerencia.entities.Guardian;
import com.app.gerencia.entities.Patient;
import com.app.gerencia.repository.GuardianRepository;
import com.app.gerencia.services.PatientService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PutMapping("/patient/{id}")
    public ResponseEntity<String> update(@RequestBody Patient patient, @PathVariable Long id){

        try{
            String response = patientService.update(patient, id);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao atualizar!");
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
}
