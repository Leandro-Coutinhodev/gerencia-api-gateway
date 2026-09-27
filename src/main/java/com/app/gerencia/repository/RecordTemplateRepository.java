package com.app.gerencia.repository;

import com.app.gerencia.entities.RecordTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecordTemplateRepository extends JpaRepository<RecordTemplate, Long> {
    List<RecordTemplate> findByProfessionalIdOrderByCreatedAtDesc(Long professionalId);
    List<RecordTemplate> findByProfessionalIdAndActiveTrueOrderByCreatedAtDesc(Long professionalId);
    List<RecordTemplate> findAllByOrderByCreatedAtDesc();
}
