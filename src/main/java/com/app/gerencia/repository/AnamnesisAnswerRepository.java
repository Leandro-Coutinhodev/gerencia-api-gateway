package com.app.gerencia.repository;

import com.app.gerencia.entities.AnamnesisAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnamnesisAnswerRepository extends JpaRepository<AnamnesisAnswer, Long> {
    List<AnamnesisAnswer> findByAnamnesisId(Long anamnesisId);
    List<AnamnesisAnswer> findByAnamnesisIdAndFieldId(Long anamnesisId, Long fieldId);

    @Modifying
    @Query("DELETE FROM AnamnesisAnswer a WHERE a.anamnesis.id = :anamnesisId")
    void deleteAllByAnamnesisId(@Param("anamnesisId") Long anamnesisId);
}