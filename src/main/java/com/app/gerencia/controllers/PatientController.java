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
    public ResponseEntity<String> save(@RequestBody Patient patient){

        try{

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
