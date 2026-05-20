package com.app.gerencia.services;

import com.app.gerencia.controllers.dto.AnamnesisResponseSubmitDTO;
import com.app.gerencia.entities.Anamnesis;
import com.app.gerencia.entities.Patient;
import com.app.gerencia.repository.AnamnesisRepository;
import com.app.gerencia.repository.PatientRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class AnamnesisService {

    private final AnamnesisRepository anamnesisRepository;
    private final PatientRepository patientRepository;
    private final AnamnesisAnswerService answerService;

    public AnamnesisService(AnamnesisRepository anamnesisRepository,
                            PatientRepository patientRepository,
                            AnamnesisAnswerService answerService) {
        this.anamnesisRepository = anamnesisRepository;
        this.patientRepository = patientRepository;
        this.answerService = answerService;
    }

    public Anamnesis save(Anamnesis anamnesis) {
        return anamnesisRepository.save(anamnesis);
    }

    @Transactional
    public Anamnesis respond(Long id,
                             AnamnesisResponseSubmitDTO dto,
                             List<MultipartFile> files) throws IOException {

        Anamnesis anamnesis = findById(id);

        if (anamnesis.getStatus() == 'A') {
            throw new IllegalStateException("Esta anamnese já foi respondida");
        }

        answerService.saveAll(anamnesis, dto, files);

        anamnesis.setStatus('A');
        return anamnesisRepository.save(anamnesis);
    }

    @Transactional(readOnly = true)
    public Anamnesis findById(Long id) {
        return anamnesisRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Anamnese não encontrada com id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Anamnesis> findByPatient(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new EntityNotFoundException("Paciente não encontrado com id: " + patientId);
        }
        return anamnesisRepository.findByPatientId(patientId);
    }

    @Transactional(readOnly = true)
    public List<Anamnesis> findAll() {
        return anamnesisRepository.findAll();
    }

    @Transactional
    public void delete(Long id) {
        if (!anamnesisRepository.existsById(id)) {
            throw new EntityNotFoundException("Anamnese não encontrada com id: " + id);
        }
        anamnesisRepository.deleteById(id);
    }
}