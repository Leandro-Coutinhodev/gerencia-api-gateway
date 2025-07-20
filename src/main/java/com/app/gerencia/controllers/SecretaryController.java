package com.app.gerencia.controllers;

import com.app.gerencia.entities.Secretary;
import com.app.gerencia.repository.SecretaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api-gateway/gerencia/user")
public class SecretaryController {

    @Autowired
    private SecretaryRepository secretaryRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/secretary")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<String> save(@RequestBody Secretary secretary){

        try{
            secretary.setPassword(passwordEncoder.encode(secretary.getPassword()));
            secretaryRepository.save(secretary);

            return new ResponseEntity<>("Salvo com sucesso", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Erro ao salvar", HttpStatus.BAD_REQUEST);
        }
    }
}
