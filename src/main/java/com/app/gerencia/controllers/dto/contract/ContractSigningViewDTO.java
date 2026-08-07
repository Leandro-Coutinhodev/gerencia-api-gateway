package com.app.gerencia.controllers.dto.contract;

import com.app.gerencia.entities.ContractParticipant;
import com.app.gerencia.enums.ParticipantRole;

import java.util.List;

public record ContractSigningViewDTO(
        Long contractId,
        String participantName,
        ParticipantRole participantRole,
        ContractParticipant.SigningStatus signingStatus,
        String renderedContent,    // HTML com variáveis já substituídas
        List<ContractTemplateDTO.AcceptFieldDTO> acceptFields
) {}
