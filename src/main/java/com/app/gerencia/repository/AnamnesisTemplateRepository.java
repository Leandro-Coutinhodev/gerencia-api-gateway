package com.app.gerencia.repository;

import com.app.gerencia.entities.AnamnesisTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnamnesisTemplateRepository extends JpaRepository<AnamnesisTemplate, Long> {
    List<AnamnesisTemplate> findByActiveTrueOrderByCreatedAtDesc();
}