package com.app.gerencia.controllers.dto;

import java.util.List;


public record AnamnesisReferralRequestDTO(
        Long anamnesisId,
        Long professionalId,
        List<Long> selectedFieldIds
) {}


