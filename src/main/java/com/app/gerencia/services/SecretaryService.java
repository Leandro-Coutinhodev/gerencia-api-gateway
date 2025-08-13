package com.app.gerencia.services;

import com.app.gerencia.entities.Secretary;
import com.app.gerencia.repository.SecretaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SecretaryService {

    @Autowired
    private SecretaryRepository secretaryRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public String save(Secretary secretary){

        secretary.setPassword(passwordEncoder.encode(secretary.getPassword()));
        secretaryRepository.save(secretary);

        return "Salvo com sucesso";
    }

    public Secretary findById(Long id){

        return secretaryRepository.findById(id).get();
    }

    public List<Secretary> findAll(){

        return secretaryRepository.findAll();
    }

    public String update(Secretary updatedSecretary, Long id) {
        Secretary existingSecretary = secretaryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assistente não encontrado"));

        // Atualiza apenas os campos modificáveis
        existingSecretary.setName(updatedSecretary.getName());
        existingSecretary.setCpf(updatedSecretary.getCpf());
        existingSecretary.setEmail(updatedSecretary.getEmail());
        existingSecretary.setPhoneNumber(updatedSecretary.getPhoneNumber());
        existingSecretary.setDateBirth(updatedSecretary.getDateBirth());

        if (updatedSecretary.getPassword() != null && !updatedSecretary.getPassword().isBlank()) {
            existingSecretary.setPassword(updatedSecretary.getPassword());
        }

        if (updatedSecretary.getPhoto() != null && updatedSecretary.getPhoto().length > 0) {
            existingSecretary.setPhoto(updatedSecretary.getPhoto());
        }

        secretaryRepository.save(existingSecretary);
        return "Atualizado com sucesso!";
    }

    public String delete(Long id){

        secretaryRepository.deleteById(id);

        return "Excluído com sucesso!";
    }
}
