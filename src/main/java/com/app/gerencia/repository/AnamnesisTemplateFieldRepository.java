package com.app.gerencia.repository;

import com.app.gerencia.entities.AnamnesisTemplateField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnamnesisTemplateFieldRepository extends JpaRepository<AnamnesisTemplateField, Long> {
    List<AnamnesisTemplateField> findByTemplateIdOrderByPositionAsc(Long templateId);
}
