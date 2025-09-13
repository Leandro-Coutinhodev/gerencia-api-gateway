package com.app.gerencia.controllers;

import com.app.gerencia.entities.Admin;
import com.app.gerencia.entities.Assistant;
import com.app.gerencia.entities.Role;
import com.app.gerencia.repository.RoleRepository;
import com.app.gerencia.services.AssistantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api-gateway/gerencia")
public class AssistantController {

    @Autowired
    private AssistantService assistantService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @PostMapping("/assistant")
    public ResponseEntity<?> save(
            @RequestParam String name,
            @RequestParam String cpf,
            @RequestParam String email,
            @RequestParam String phoneNumber,
            @RequestParam String password,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateBirth,
            @RequestParam(required = false) MultipartFile photo,
            @RequestParam String position,
            @RequestParam String affiliation
    ) throws Exception {

        Assistant assistant = new Assistant();
        assistant.setName(name);
        assistant.setCpf(cpf);
        assistant.setEmail(email);
        assistant.setPhoneNumber(phoneNumber);
        assistant.setPassword(passwordEncoder.encode(password));
        assistant.setPosition(position);
        assistant.setAffiliation(affiliation);

        // conversão da data
        assistant.setDateBirth(java.sql.Date.valueOf(dateBirth));

        if (photo != null && !photo.isEmpty()) {
            assistant.setPhoto(photo.getBytes());
        }
        Role assistantRole = roleRepository.findByName("ASSISTANT");
        if (assistantRole == null) {
            throw new RuntimeException("Role 'ASSISTANT' não encontrada");
        }

        assistant.setRoles(Set.of(assistantRole));
        assistantService.save(assistant);
        return ResponseEntity.ok("Usuário criado com sucesso");

    }

    @GetMapping("/assistant/{id}")
    public ResponseEntity<Assistant> findById(@PathVariable Long id){
        try {

            Assistant assistant = assistantService.findById(id);
            return new ResponseEntity<>(assistant, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/assistant")
    public ResponseEntity<List<Assistant>> findAll(){
        try {

            List<Assistant> assistants = assistantService.findAll();
            return new ResponseEntity<>(assistants, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/assistant/{id}")
    public ResponseEntity<?> update(
            @RequestParam String name,
            @RequestParam String cpf,
            @RequestParam String email,
            @RequestParam String phoneNumber,
            @RequestParam(required = false) String password,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateBirth,
            @RequestParam(required = false) MultipartFile photo,
            @RequestParam String position,
            @RequestParam String affiliation,
            @PathVariable Long id
    ) throws Exception {

        Assistant assistant = assistantService.findById(id);
        if (assistant == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Assistente não encontrado");
        }

        assistant.setName(name);
        assistant.setCpf(cpf);
        assistant.setEmail(email);
        assistant.setPhoneNumber(phoneNumber);
        assistant.setPosition(position);
        assistant.setAffiliation(affiliation);

        if (password != null && !password.isBlank()) {
            assistant.setPassword(passwordEncoder.encode(password));
        }

        // Converte e atualiza a data
        assistant.setDateBirth(java.sql.Date.valueOf(dateBirth));

        if (photo != null && !photo.isEmpty()) {
            assistant.setPhoto(photo.getBytes());
        }

        Role assistantRole = roleRepository.findByName("ASSISTANT");
        if (assistantRole == null) {
            throw new RuntimeException("Role 'ASSISTANT' não encontrada");
        }
        Set<Role> roles = new HashSet<>();
        roles.add(assistantRole);
        assistant.setRoles(roles);


        assistantService.update(assistant, id);
        return ResponseEntity.ok("Usuário atualizado com sucesso");
    }


    @DeleteMapping("/assistant/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id){
        try {

            String response = assistantService.delete(id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Erro ao deletar", HttpStatus.BAD_REQUEST);
        }
    }
}