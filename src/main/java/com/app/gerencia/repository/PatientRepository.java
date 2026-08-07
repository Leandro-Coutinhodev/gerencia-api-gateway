package com.app.gerencia.repository;

import com.app.gerencia.entities.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    List<Patient> findByNameContainingIgnoreCase(String name);
    List<Patient> findByCpfContainingIgnoreCase(String cpf);
    List<Patient> findByGuardianId(Long guardianId);
    List<Patient> findByNameContainingIgnoreCaseOrCpfContaining(String name, String cpf);
    @Query("SELECT p FROM Patient p LEFT JOIN FETCH p.guardian WHERE MONTH(p.dateBirth) = :month ORDER BY DAY(p.dateBirth) ASC")
    List<Patient> findByBirthMonth(@Param("month") int month);
}
