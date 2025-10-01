package com.app.gerencia.controllers.dto;

import com.app.gerencia.entities.Anamnesis;

import java.util.Date;

public record AnamnesisResponseDTO(
        Long id,
        String patientName,
        String guardianName,
        String guardianPhone,
        Date interviewDate,
        String status
) {
    public static AnamnesisResponseDTO fromEntity(Anamnesis anamnesis) {
        return new AnamnesisResponseDTO(
                anamnesis.getId(),
                anamnesis.getPatient().getName(),
                anamnesis.getPatient().getGuardian() != null ? anamnesis.getPatient().getGuardian().getName() : null,
                anamnesis.getPatient().getGuardian() != null ? anamnesis.getPatient().getGuardian().getPhoneNumber() : null,
                anamnesis.getInterviewDate(),
                mapStatus(anamnesis.getStatus())
        );
    }

    private static String mapStatus(Character status) {
        return switch (status) {
            case 'E' -> "Encaminhada";
            case 'P' -> "Preenchida";
            default -> "Desconhecido";
        };
    }
}
