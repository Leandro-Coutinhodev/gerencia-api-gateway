package com.app.gerencia.controllers.dto;

import com.app.gerencia.entities.AnamnesisReferral;
import java.util.Date;

public record AnamnesisReferralResponseDTO(
        Long id,
        String patientName,
        Long patientId,
        String professionalName,
        String guardianName,
        Long anamnesisId,
        Long professionalId,
        Long senderId,
        String selectedFieldsJson,
        Date sentAt
) {
    public static AnamnesisReferralResponseDTO fromEntity(AnamnesisReferral referral) {
        var patient = referral.getAnamnesis() != null ? referral.getAnamnesis().getPatient() : null;
        return new AnamnesisReferralResponseDTO(
                referral.getId(),
                patient != null ? patient.getName() : null,
                patient != null ? patient.getId() : null,
                referral.getProfessional() != null ? referral.getProfessional().getName() : null,
                patient != null && patient.getGuardian() != null ? patient.getGuardian().getName() : null,
                referral.getAnamnesis() != null ? referral.getAnamnesis().getId() : null,
                referral.getProfessional() != null ? referral.getProfessional().getId() : null,
                referral.getSender() != null ? referral.getSender().getId() : null,
                referral.getSelectedFieldsJson(),
                referral.getSentAt()
        );
    }
}
