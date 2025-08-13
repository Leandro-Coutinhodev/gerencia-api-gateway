package com.app.gerencia.controllers;

import com.app.gerencia.entities.User;
import com.app.gerencia.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api-gateway/gerencia")
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/user")
    public ResponseEntity<String> save(@RequestBody User user){

        try {

            String response = userService.save(user);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Erro ao registrar novo usuário", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id){

        try{

            User userData = userService.findById(id);
            return new ResponseEntity<>(userData, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/user")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<List<User>> getAll(){

        try{

            List<User> usersList = userService.getAll();
            return new ResponseEntity<>(usersList, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/user/{id}")
    public ResponseEntity<String> update(@RequestBody User user, @PathVariable Long id){

        try{

            String response = userService.update(user, id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Erro ao atualizar usuário", HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id){

        try{

            String response = userService.delete(id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Erro ao deletar usuário", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/user/{id}/photo")
    public ResponseEntity<byte[]> findUserPhoto(@PathVariable Long id){

        User user = userService.findById(id);

        if (user != null && user.getPhoto() != null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)  // ou conforme seu tipo
                    .body(user.getPhoto());
        }
            return ResponseEntity.notFound().build();
    }

}
