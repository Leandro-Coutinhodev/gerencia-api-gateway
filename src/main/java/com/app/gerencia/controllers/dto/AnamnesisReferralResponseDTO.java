package com.app.gerencia.controllers.dto;

import com.app.gerencia.entities.AnamnesisReferral;
import java.util.Date;

public record AnamnesisReferralResponseDTO(
        Long id,
        String patientName,
        Long anamnesisId,
        Long assistantId,
        Long professionalId,
        String selectedFieldsJson,
        Date sentAt
) {
    public static AnamnesisReferralResponseDTO fromEntity(AnamnesisReferral referral) {
        return new AnamnesisReferralResponseDTO(
                referral.getId(),
                referral.getAnamnesis() != null ? referral.getAnamnesis().getPatient().getName() : null,
                referral.getAnamnesis() != null ? referral.getAnamnesis().getId() : null,
                referral.getAssistant() != null ? referral.getAssistant().getId() : null,
                referral.getProfessional() != null ? referral.getProfessional().getId() : null,
                referral.getSelectedFieldsJson(),
                referral.getSentAt()
        );
    }
}
