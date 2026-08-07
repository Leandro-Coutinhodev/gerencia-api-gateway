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
        return new AnamnesisReferralResponseDTO(
                referral.getId(),
                referral.getAnamnesis() != null ? referral.getAnamnesis().getPatient().getName() : null,
                referral.getAnamnesis() != null ? referral.getAnamnesis().getPatient().getId() : null,
                referral.getProfessional() != null ? referral.getProfessional().getName() : null,
                referral.getAnamnesis() != null ? referral.getAnamnesis().getPatient().getGuardian().getName() : null,
                referral.getAnamnesis() != null ? referral.getAnamnesis().getId() : null,
                referral.getProfessional() != null ? referral.getProfessional().getId() : null,
                referral.getSender() != null ? referral.getSender().getId() : null,
                referral.getSelectedFieldsJson(),
                referral.getSentAt()
        );
    }
}
