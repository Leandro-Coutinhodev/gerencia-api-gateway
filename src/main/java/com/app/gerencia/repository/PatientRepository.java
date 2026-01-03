package com.app.gerencia.repository;

import com.app.gerencia.entities.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    List<Patient> findByNameContainingIgnoreCase(String name);
    List<Patient> findByCpfContainingIgnoreCase(String cpf);
    List<Patient> findByGuardianId(Long guardianId);
    List<Patient> findByNameContainingIgnoreCaseOrCpfContaining(String name, String cpf);
}
