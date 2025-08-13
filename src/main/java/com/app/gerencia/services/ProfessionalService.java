package com.app.gerencia.services;

import com.app.gerencia.entities.Professional;
import com.app.gerencia.repository.ProfessionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfessionalService {

    @Autowired
    private ProfessionalRepository professionalRepository;

    public String save(Professional professional){

        professionalRepository.save(professional);
        return "Salvo com sucesso!";
    }

    public Professional findById(Long id){

        return professionalRepository.findById(id).get();
    }

    public List<Professional> findAll(){

        return professionalRepository.findAll();
    }

    public String update(Professional updatedProfessional, Long id) {
        Professional existingProfessional = professionalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assistente não encontrado"));

        // Atualiza apenas os campos modificáveis
        existingProfessional.setName(updatedProfessional.getName());
        existingProfessional.setCpf(updatedProfessional.getCpf());
        existingProfessional.setEmail(updatedProfessional.getEmail());
        existingProfessional.setPhoneNumber(updatedProfessional.getPhoneNumber());
        existingProfessional.setDateBirth(updatedProfessional.getDateBirth());
        existingProfessional.setProfessionalLicense(updatedProfessional.getProfessionalLicense());

        if (updatedProfessional.getPassword() != null && !updatedProfessional.getPassword().isBlank()) {
            existingProfessional.setPassword(updatedProfessional.getPassword());
        }

        if (updatedProfessional.getPhoto() != null && updatedProfessional.getPhoto().length > 0) {
            existingProfessional.setPhoto(updatedProfessional.getPhoto());
        }

        professionalRepository.save(existingProfessional);
        return "Atualizado com sucesso!";
    }

    public String delete(Long id){

        professionalRepository.deleteById(id);
        return "Excluído com sucesso!";
    }
}
