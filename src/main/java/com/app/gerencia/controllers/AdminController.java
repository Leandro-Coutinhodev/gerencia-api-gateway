package com.app.gerencia.controllers;

import com.app.gerencia.entities.Admin;
import com.app.gerencia.entities.Assistant;
import com.app.gerencia.entities.Role;
import com.app.gerencia.repository.RoleRepository;
import com.app.gerencia.services.AdminService;
import com.sun.jdi.event.StepEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("api-gateway/gerencia")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @PostMapping(value = "/admin", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> save(
            @RequestParam String name,
            @RequestParam String cpf,
            @RequestParam String email,
            @RequestParam String phoneNumber,
            @RequestParam String password,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateBirth,
            @RequestParam(required = false) MultipartFile photo
    ) throws Exception {

        Admin admin = new Admin();
        admin.setName(name);
        admin.setCpf(cpf);
        admin.setEmail(email);
        admin.setPhoneNumber(phoneNumber);
        admin.setPassword(passwordEncoder.encode(password));

        // conversão da data
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        admin.setDateBirth(Date.valueOf(dateBirth));

        if (photo != null && !photo.isEmpty()) {
            admin.setPhoto(photo.getBytes());
        }
        Role adminRole = roleRepository.findByName("ADMIN");
        if (adminRole == null) {
            throw new RuntimeException("Role 'ROLE_ADMIN' não encontrada");
        }

        admin.setRoles(Set.of(adminRole));
        adminService.save(admin);
        return ResponseEntity.ok("Usuário criado com sucesso");
    }


    @GetMapping("/admin/{id}")
    public ResponseEntity<Admin> findById(@PathVariable Long id){
        try {

            Admin admin = adminService.findById(id);
            return new ResponseEntity<>(admin, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/admins")
    public ResponseEntity<List<Admin>> findAll(){
        try {

            List<Admin> admins = adminService.findAll();
            return new ResponseEntity<>(admins, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id){
        try {

            String response = adminService.delete(id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Erro ao deletar", HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/admin/{id}")
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

        Admin admin = adminService.findById(id);
        if (admin == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Administrador não encontrado");
        }

        admin.setName(name);
        admin.setCpf(cpf);
        admin.setEmail(email);
        admin.setPhoneNumber(phoneNumber);

        if (password != null && !password.isBlank()) {
            admin.setPassword(passwordEncoder.encode(password));
        }

        // Converte e atualiza a data
        admin.setDateBirth(Date.valueOf(dateBirth));

        if (photo != null && !photo.isEmpty()) {
            admin.setPhoto(photo.getBytes());
        }

        Role adminRole = roleRepository.findByName("ADMIN");
        if (adminRole == null) {
            throw new RuntimeException("Role 'ADMIN' não encontrada");
        }
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        admin.setRoles(roles);
    

        adminService.update(admin, id);
        return ResponseEntity.ok("Usuário atualizado com sucesso");
    }


}
