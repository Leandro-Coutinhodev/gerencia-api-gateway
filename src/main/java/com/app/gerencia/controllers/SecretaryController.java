package com.app.gerencia.controllers;

import com.app.gerencia.entities.Role;
import com.app.gerencia.entities.Secretary;
import com.app.gerencia.repository.RoleRepository;
import com.app.gerencia.services.SecretaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("api-gateway/gerencia/")
public class SecretaryController {

    @Autowired
    private SecretaryService secretaryService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @PostMapping("/secretary")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<?> save(
            @RequestParam String name,
            @RequestParam String cpf,
            @RequestParam String email,
            @RequestParam String phoneNumber,
            @RequestParam String password,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateBirth,
            @RequestParam(required = false) MultipartFile photo
    ) throws Exception {

        Secretary secretary = new Secretary();
        secretary.setName(name);
        secretary.setCpf(cpf);
        secretary.setEmail(email);
        secretary.setPhoneNumber(phoneNumber);
        secretary.setPassword(password);

        // conversão da data
        secretary.setDateBirth(Date.valueOf(dateBirth));

        if (photo != null && !photo.isEmpty()) {
            secretary.setPhoto(photo.getBytes());
        }
        Role secretaryRole = roleRepository.findByName("SECRETARY");
        if (secretaryRole == null) {
            throw new RuntimeException("Role 'SECRETARY' não encontrada");
        }

        secretary.setRoles(Set.of(secretaryRole));
        secretaryService.save(secretary);
        return ResponseEntity.ok("Usuário criado com sucesso");

    }

    @GetMapping("/secretary/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<Secretary> findById(@PathVariable Long id){

        try {

            Secretary secretary = secretaryService.findById(id);
            return new ResponseEntity<>(secretary, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/secretary")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_SECRETARY')")
    public ResponseEntity<List<Secretary>> findAll(){

        try {

            List<Secretary> secretaries = secretaryService.findAll();
            return new ResponseEntity<>(secretaries, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/secretary/{id}")
    public ResponseEntity<?> update(
            @RequestParam String name,
            @RequestParam String cpf,
            @RequestParam String email,
            @RequestParam String phoneNumber,
            @RequestParam(required = false) String password,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateBirth,
            @RequestParam(required = false) MultipartFile photo,
            @PathVariable Long id
    ) throws Exception {

        Secretary secretary = secretaryService.findById(id);
        if (secretary == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Assistente não encontrado");
        }

        secretary.setName(name);
        secretary.setCpf(cpf);
        secretary.setEmail(email);
        secretary.setPhoneNumber(phoneNumber);

        if (password != null && !password.isBlank()) {
            secretary.setPassword(passwordEncoder.encode(password));
        }

        // Converte e atualiza a data
        secretary.setDateBirth(Date.valueOf(dateBirth));

        if (photo != null && !photo.isEmpty()) {
            secretary.setPhoto(photo.getBytes());
        }

        Role secretaryRole = roleRepository.findByName("SECRETARY");
        if (secretaryRole == null) {
            throw new RuntimeException("Role 'SECRETARY' não encontrada");
        }
        Set<Role> roles = new HashSet<>();
        roles.add(secretaryRole);
        secretary.setRoles(roles);


        secretaryService.update(secretary, id);
        return ResponseEntity.ok("Usuário atualizado com sucesso");
    }

    @DeleteMapping("/secretary/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id){
        try {

            String response = secretaryService.delete(id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Erro ao deletar", HttpStatus.BAD_REQUEST);
        }
    }
}
