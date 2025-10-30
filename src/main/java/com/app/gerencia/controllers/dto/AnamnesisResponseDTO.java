package com.app.gerencia.controllers.dto;

import com.app.gerencia.entities.Anamnesis;

import java.util.Date;

public record AnamnesisResponseDTO(
        Long id,
        Long patientId,
        String patientName,
        String guardianName,
        String guardianPhone,
        Date interviewDate,
        String status,
        String link
) {
    public static AnamnesisResponseDTO fromEntity(Anamnesis anamnesis, String token) {
        String link = "http://localhost:3000/form-anamnese/" + token;

        return new AnamnesisResponseDTO(
                anamnesis.getId(),
                anamnesis.getPatient().getId(),
                anamnesis.getPatient().getName(),
                anamnesis.getPatient().getGuardian() != null ? anamnesis.getPatient().getGuardian().getName() : null,
                anamnesis.getPatient().getGuardian() != null ? anamnesis.getPatient().getGuardian().getPhoneNumber1() : null,
                anamnesis.getInterviewDate(),
                mapStatus(anamnesis.getStatus()),
                link
        );
    }

    private static String mapStatus(Character status) {
        return switch (status) {
            case 'E' -> "Encaminhada";
            case 'A' -> "Análise";
            default -> "Desconhecido";
        };
    }
}
