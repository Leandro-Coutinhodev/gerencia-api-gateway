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
        String report,
        String status,
        String link
) {
    public AnamnesisDTO(Anamnesis anamnesis, String token) {
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
                anamnesis.getReport() != null ? Base64.getEncoder().encodeToString(anamnesis.getReport()) : null,
                mapStatus(anamnesis.getStatus()),
                "http://localhost:3000/form-anamnese/" + token  // Link gerado diretamente aqui
        );
    }
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
                mapStatus(anamnesis.getStatus()),
                anamnesis.getReport() != null ? Base64.getEncoder().encodeToString(anamnesis.getReport()) : null,
                null
        );
    }
    private static String mapStatus(Character status) {
        return switch (status) {
            case 'E' -> "Encaminhada";
            case 'A' -> "Análise";
            case 'P' -> "Pronto";
            default -> "Desconhecido";
        };
    }
}