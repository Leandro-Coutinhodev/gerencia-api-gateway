package com.app.gerencia.services;

import com.app.gerencia.entities.Guardian;
import com.app.gerencia.repository.GuardianRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GuardianService {

    @Autowired
    private GuardianRepository guardianRepository;

    public String save(Guardian guardian){

        guardianRepository.save(guardian);
        return "Salvo com sucesso!";
    }


    public List<Guardian> findByCpf(String cpf) {
        return guardianRepository.findByCpfContaining(cpf);
    }

    public List<Guardian> findByName(String name) {
        return guardianRepository.findByNameContainingIgnoreCase(name);
    }

    public Guardian findById(Long id){

        return guardianRepository.findById(id).get();
    }

    public List<Guardian> findAll(){

        return guardianRepository.findAll();
    }

    public String update(Guardian guardian, Long id){

        guardian.setId(id);
        guardianRepository.save(guardian);

        return "Atualizado com sucesso!";
    }

    public String delete(Long id){

        guardianRepository.deleteById(id);

        return "Excluído com sucesso!";
    }
}
