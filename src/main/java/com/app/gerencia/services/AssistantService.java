package com.app.gerencia.services;

import com.app.gerencia.entities.Assistant;
import com.app.gerencia.repository.AssistantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssistantService {

    @Autowired
    private AssistantRepository assistantRepository;

    public String save(Assistant assistant){

        assistantRepository.save(assistant);
        return "Salvo com sucesso!";
    }

    public Assistant findById(Long id){

        return assistantRepository.findById(id).get();
    }

    public List<Assistant> findAll(){

        return assistantRepository.findAll();
    }

    public String update(Assistant updatedAssistant, Long id) {
        Assistant existingAssistant = assistantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assistente não encontrado"));

        // Atualiza apenas os campos modificáveis
        existingAssistant.setName(updatedAssistant.getName());
        existingAssistant.setCpf(updatedAssistant.getCpf());
        existingAssistant.setEmail(updatedAssistant.getEmail());
        existingAssistant.setPhoneNumber(updatedAssistant.getPhoneNumber());
        existingAssistant.setPosition(updatedAssistant.getPosition());
        existingAssistant.setDateBirth(updatedAssistant.getDateBirth());

        if (updatedAssistant.getPassword() != null && !updatedAssistant.getPassword().isBlank()) {
            existingAssistant.setPassword(updatedAssistant.getPassword());
        }

        if (updatedAssistant.getPhoto() != null && updatedAssistant.getPhoto().length > 0) {
            existingAssistant.setPhoto(updatedAssistant.getPhoto());
        }

        assistantRepository.save(existingAssistant);
        return "Atualizado com sucesso!";
    }


    public String delete(Long id){

        assistantRepository.deleteById(id);
        return "Excluído com sucesso!";
    }
}
