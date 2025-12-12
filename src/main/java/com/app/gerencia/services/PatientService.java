package com.app.gerencia.services;

import com.app.gerencia.entities.Guardian;
import com.app.gerencia.entities.Patient;
import com.app.gerencia.repository.GuardianRepository;
import com.app.gerencia.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public String update(Patient patient, Long id){
        Patient existing = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado"));

        // Atualiza dados do paciente
        existing.setName(patient.getName());
        existing.setCpf(patient.getCpf());
        existing.setDateBirth(patient.getDateBirth());
        existing.setKinship(patient.getKinship());

        // Atualiza ou cadastra o responsável
        if (patient.getGuardian() != null) {
            Guardian guardianData = patient.getGuardian();

            Guardian guardian;
            System.out.println("ENCONTRADO!  " + guardianData.getId());
            if (guardianData.getId() != null) {
                guardian = guardianRepository.findById(guardianData.getId())
                        .orElseThrow(() -> new RuntimeException("Responsável não encontrado"));
                guardian.setName(guardianData.getName());
                guardian.setCpf(guardianData.getCpf());
                guardian.setEmail(guardianData.getEmail());
                guardian.setPhoneNumber1(guardianData.getPhoneNumber1());
                guardian.setPhoneNumber2(guardianData.getPhoneNumber2());
                guardian.setAddressLine1(guardianData.getAddressLine1());
                guardian.setDateBirth(guardianData.getDateBirth());
                guardian.setCep(guardianData.getCep());
                guardian.setState(guardianData.getState());
                guardian.setCity(guardianData.getCity());
                guardian.setNumber(guardianData.getNumber());
                guardian.setNeighborhood(guardianData.getNeighborhood());
                guardian.setAddressLine2(guardianData.getAddressLine2());

                guardian = guardianRepository.save(guardian);
            } else {
                System.out.println("ENCONTRADO");
                guardian = guardianRepository.save(guardianData);
            }

            existing.setGuardian(guardian);
        }

        patientRepository.save(existing);

        return "Atualizado com sucesso!";
    }

    public String delete(Long id){

        patientRepository.deleteById(id);

        return "Excluído com sucesso!";
    }

    public List<Patient> searchByGuardian(Long id){

        return patientRepository.findByGuardianId(id);
    }
}
