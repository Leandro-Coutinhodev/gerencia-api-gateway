package com.app.gerencia.services;

import com.app.gerencia.entities.Admin;
import com.app.gerencia.entities.Assistant;
import com.app.gerencia.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    public String save(Admin admin){

        adminRepository.save(admin);
        return "Salvo com sucesso!";
    }

    public Admin findById(Long id){

        return adminRepository.findById(id).get();
    }

    public List<Admin> findAll(){

        return adminRepository.findAll();
    }

    public String update(Admin updatedAdmin, Long id) {
        Admin existingAdmin = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Administrador não encontrado"));

        // Atualiza apenas os campos modificáveis
        existingAdmin.setName(updatedAdmin.getName());
        existingAdmin.setCpf(updatedAdmin.getCpf());
        existingAdmin.setEmail(updatedAdmin.getEmail());
        existingAdmin.setPhoneNumber(updatedAdmin.getPhoneNumber());
        existingAdmin.setDateBirth(updatedAdmin.getDateBirth());

        if (updatedAdmin.getPassword() != null && !updatedAdmin.getPassword().isBlank()) {
            existingAdmin.setPassword(updatedAdmin.getPassword());
        }

        if (updatedAdmin.getPhoto() != null && updatedAdmin.getPhoto().length > 0) {
            existingAdmin.setPhoto(updatedAdmin.getPhoto());
        }

        adminRepository.save(existingAdmin);
        return "Atualizado com sucesso!";
    }

    public String delete(Long id){

        adminRepository.deleteById(id);
        return "Excluído com sucesso!";
    }
}
