package com.app.gerencia.controllers.dto;

import com.app.gerencia.entities.Anamnesis;

import java.util.Base64;
import java.util.Date;

public record AnamnesisDTO(
        Long id,
        Long patientId,
        String patientName,
        Date interviewDate,
        String diagnoses,
        String medicationAndAllergies,
        String indications,
        String objectives,
        String developmentHistory,
        String preferences,
        String interferingBehaviors,
        String qualityOfLife,
        String feeding,
        String sleep,
        String therapists,
        String report
) {
    public AnamnesisDTO(Anamnesis anamnesis) {
        this(
                anamnesis.getId(),
                anamnesis.getPatient() != null ? anamnesis.getPatient().getId() : null,
                anamnesis.getPatient() != null ? anamnesis.getPatient().getName() : null,
                anamnesis.getInterviewDate(),
                anamnesis.getDiagnoses(),
                anamnesis.getMedicationAndAllergies(),
                anamnesis.getIndications(),
                anamnesis.getObjectives(),
                anamnesis.getDevelopmentHistory(),
                anamnesis.getPreferences(),
                anamnesis.getInterferingBehaviors(),
                anamnesis.getQualityOfLife(),
                anamnesis.getFeeding(),
                anamnesis.getSleep(),
                anamnesis.getTherapists(),
                anamnesis.getReport() != null ? Base64.getEncoder().encodeToString(anamnesis.getReport()) : null
        );
    }
}