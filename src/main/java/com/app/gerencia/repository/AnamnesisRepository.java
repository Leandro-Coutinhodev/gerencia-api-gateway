package com.app.gerencia.repository;

import com.app.gerencia.entities.Anamnesis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnamnesisRepository extends JpaRepository<Anamnesis, Long> {
    List<Anamnesis> findByPatientId(Long patientId);
}
