package com.app.gerencia.services;

import com.app.gerencia.entities.Anamnesis;
import com.app.gerencia.entities.Patient;
import com.app.gerencia.repository.AnamnesisRepository;
import com.app.gerencia.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AnamnesisService {

    private final AnamnesisRepository anamnesisRepository;
    private final PatientRepository patientRepository;

    public AnamnesisService(AnamnesisRepository anamnesisRepository, PatientRepository patientRepository) {
        this.anamnesisRepository = anamnesisRepository;
        this.patientRepository = patientRepository;
    }
    public Anamnesis save(Anamnesis anamnesis) {
        return anamnesisRepository.save(anamnesis);
    }


    public Anamnesis save(Anamnesis anamnesis, Long patientId) {
        Optional<Patient> patient = patientRepository.findById(patientId);
        if (patient.isEmpty()) {
            throw new RuntimeException("Patient not found with id " + patientId);
        }
        anamnesis.setPatient(patient.get());
        return anamnesisRepository.save(anamnesis);
    }

    public List<Anamnesis> findByPatient(Long patientId) {
        return anamnesisRepository.findByPatientId(patientId);
    }

    public List<Anamnesis> findAll() {
        return anamnesisRepository.findAll();
    }

    public String delete(Long id){
        anamnesisRepository.deleteById(id);

        return "Deletado com sucesso";
    }
}
