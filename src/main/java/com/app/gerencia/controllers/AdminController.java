package com.app.gerencia.controllers;

import com.app.gerencia.entities.Admin;
import com.app.gerencia.services.AdminService;
import com.sun.jdi.event.StepEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api-gateway/gerencia")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/admin")
    public ResponseEntity<String> save(@RequestBody Admin admin){

        try {
         String retorno = adminService.save(admin);

         return new ResponseEntity<>(retorno, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Erro ao cadastrar", HttpStatus.BAD_REQUEST);
        }
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

            String retorno = adminService.delete(id);
            return new ResponseEntity<>(retorno, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Erro ao deletar", HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<String> update(@RequestBody Admin admin, @PathVariable Long id){
        try {

            String retorno = adminService.update(admin, id);
            return new ResponseEntity<>(retorno, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Erro ao atualizar", HttpStatus.BAD_REQUEST);
        }
    }


}
