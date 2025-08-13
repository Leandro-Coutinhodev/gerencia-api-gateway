package com.app.gerencia.controllers;

import com.app.gerencia.entities.Professional;
import com.app.gerencia.entities.Role;
import com.app.gerencia.entities.Secretary;
import com.app.gerencia.repository.RoleRepository;
import com.app.gerencia.services.ProfessionalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api-gateway/gerencia")
public class ProfessionalController {

    @Autowired
    private ProfessionalService professionalService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @PostMapping("/professional")
    public ResponseEntity<?> save(
            @RequestParam String name,
            @RequestParam String cpf,
            @RequestParam String email,
            @RequestParam String phoneNumber,
            @RequestParam String password,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateBirth,
            @RequestParam(required = false) MultipartFile photo,
            @RequestParam String professionalLicense
    ) throws Exception {

        Professional professional = new Professional();
        professional.setName(name);
        professional.setCpf(cpf);
        professional.setEmail(email);
        professional.setPhoneNumber(phoneNumber);
        professional.setPassword(passwordEncoder.encode(password));
        professional.setProfessionalLicense(professionalLicense);

        // conversão da data
        professional.setDateBirth(Date.valueOf(dateBirth));

        if (photo != null && !photo.isEmpty()) {
            professional.setPhoto(photo.getBytes());
        }
        Role professionalRole = roleRepository.findByName("PROFESSIONAL");
        if (professionalRole == null) {
            throw new RuntimeException("Role 'PROFESSIONAL' não encontrada");
        }

        professional.setRoles(Set.of(professionalRole));
        professionalService.save(professional);
        return ResponseEntity.ok("Usuário criado com sucesso");

    }

    @GetMapping("/professional/{id}")
    public ResponseEntity<Professional> findById(@PathVariable Long id){
        try {

            Professional professional = professionalService.findById(id);
            return new ResponseEntity<>(professional, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/professional")
    public ResponseEntity<List<Professional>> findAll(){
        try {

            List<Professional> professionals = professionalService.findAll();
            return new ResponseEntity<>(professionals, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/professional/{id}")
    public ResponseEntity<?> update(
            @RequestParam String name,
            @RequestParam String cpf,
            @RequestParam String email,
            @RequestParam String phoneNumber,
            @RequestParam(required = false) String password,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateBirth,
            @RequestParam(required = false) MultipartFile photo,
            @RequestParam String professionalLicense,
            @PathVariable Long id
    ) throws Exception {

        Professional professional = professionalService.findById(id);
        if (professional == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Profissional não encontrado");
        }

        professional.setName(name);
        professional.setCpf(cpf);
        professional.setEmail(email);
        professional.setPhoneNumber(phoneNumber);
        professional.setProfessionalLicense(professionalLicense);

        if (password != null && !password.isBlank()) {
            professional.setPassword(passwordEncoder.encode(password));
        }

        // Converte e atualiza a data
        professional.setDateBirth(Date.valueOf(dateBirth));

        if (photo != null && !photo.isEmpty()) {
            professional.setPhoto(photo.getBytes());
        }

        Role professionalRole = roleRepository.findByName("PROFESSIONAL");
        if (professionalRole == null) {
            throw new RuntimeException("Role 'PROFESSIONAL' não encontrada");
        }
        Set<Role> roles = new HashSet<>();
        roles.add(professionalRole);
        professional.setRoles(roles);


        professionalService.update(professional, id);
        return ResponseEntity.ok("Usuário atualizado com sucesso");
    }

    @DeleteMapping("/professional/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id){
        try {

            String response = professionalService.delete(id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Erro ao deletar", HttpStatus.BAD_REQUEST);
        }
    }


}
