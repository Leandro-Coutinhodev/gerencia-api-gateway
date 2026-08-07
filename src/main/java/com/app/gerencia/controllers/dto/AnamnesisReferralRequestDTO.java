package com.app.gerencia.controllers.dto;

import java.util.List;

// AnamnesisReferralRequestDTO
public record AnamnesisReferralRequestDTO(
        Long anamnesisId,
        Long professionalId,
        List<Long> selectedFieldIds
) {}


