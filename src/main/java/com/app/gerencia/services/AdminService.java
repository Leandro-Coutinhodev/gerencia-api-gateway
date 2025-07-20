package com.app.gerencia.services;

import com.app.gerencia.entities.Admin;
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

    public String update(Admin admin, Long id){

        admin.setId(id);
        adminRepository.save(admin);
        return "Atualizado com sucesso!";
    }

    public String delete(Long id){

        adminRepository.deleteById(id);
        return "Excluído com sucesso!";
    }
}
