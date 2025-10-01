package com.app.gerencia.controllers;

import com.app.gerencia.entities.Guardian;
import com.app.gerencia.services.GuardianService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api-gateway/gerencia")
public class GuardianController {

    @Autowired
    private GuardianService guardianService;

    @PostMapping("/guardian")
    public ResponseEntity<String> save(@RequestBody Guardian guardian){

        try{
            String response = guardianService.save(guardian);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return  ResponseEntity.badRequest().body("Erro ao salvar!");
        }
    }

    @GetMapping("/guardian/{id}")
    public ResponseEntity<Guardian> findById(@PathVariable Long id){

        try{
            Guardian guardian = guardianService.findById(id);

            return ResponseEntity.ok(guardian);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/guardian/")
    public ResponseEntity<List<Guardian>> findByCpf(@RequestParam(required = false) String cpf) {
        try {
            List<Guardian> guardians = guardianService.findByCpf(cpf);
            return ResponseEntity.ok(guardians);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/guardian/{id}")
    public ResponseEntity<String> update(@RequestBody Guardian guardian, @PathVariable Long id){

        try{
            String response = guardianService.update(guardian, id);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao salvar!");
        }
    }

    @DeleteMapping("/guardian/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id){

        try {
            String response = guardianService.delete(id);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao excluir!");
        }
    }

}
