package com.app.gerencia.services;

import com.app.gerencia.entities.Guardian;
import com.app.gerencia.entities.Patient;
import com.app.gerencia.repository.GuardianRepository;
import com.app.gerencia.repository.PatientRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private GuardianRepository guardianRepository;

    public String save(Patient patient) {
        if (patient.getGuardian() != null) {
            Guardian guardianData = patient.getGuardian();
            Guardian guardian;

            if (guardianData.getId() != null) {
                guardian = guardianRepository.findById(guardianData.getId())
                        .orElseThrow(() -> new RuntimeException("Responsável não encontrado"));
            } else {
                guardian = guardianRepository.save(guardianData);
            }

            patient.setGuardian(guardian);
        }

        patientRepository.save(patient);
        return "Salvo com sucesso!";
    }

    public List<Patient> findByCpf(String cpf){
        return patientRepository.findByCpfContainingIgnoreCase(cpf);
    }

    public Patient findById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado com id " + id));
    }


    public List<Patient> findByName(String nome) {
        return patientRepository.findByNameContainingIgnoreCase(nome);
    }

    public List<Patient> findAll(){

        return patientRepository.findAll();
    }

    @Transactional
    public String update(Patient patient, Long id) {

        Patient existingPatient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado"));


        existingPatient.setName(patient.getName());
        existingPatient.setCpf(patient.getCpf());
        existingPatient.setDateBirth(patient.getDateBirth());
        existingPatient.setKinship(patient.getKinship());


        if (patient.getGuardian() != null) {
            existingPatient.setGuardian(patient.getGuardian());
        }


        if (patient.getPhoto() != null && patient.getPhoto().length > 0) {
            existingPatient.setPhoto(patient.getPhoto());
        }


        patientRepository.save(existingPatient);

        return "Paciente atualizado com sucesso!";
    }

    public String delete(Long id){

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado"));

        patientRepository.delete(patient);


        return "Excluído com sucesso!";
    }

    public List<Patient> searchByGuardian(Long id){

        return patientRepository.findByGuardianId(id);
    }
    public List<Patient> searchByNameOrCpf(String query) {

        String cleanQuery = query.replaceAll("[^0-9]", "");


        if (cleanQuery.length() >= 3) {

            return patientRepository.findByNameContainingIgnoreCaseOrCpfContaining(query, query);
        } else {

            return patientRepository.findByNameContainingIgnoreCase(query);
        }
    }

    public List<Patient> findByBirthMonth(Integer month) {
        int targetMonth = (month != null) ? month : LocalDate.now().getMonthValue();
        return patientRepository.findByBirthMonth(targetMonth);
    }
}
