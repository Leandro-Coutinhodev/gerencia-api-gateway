package com.app.gerencia.controllers.dto.contract;

import java.util.List;

public record CreateContractForSigningRequestDTO(
        Long templateId,
        Long patientId,
        Long guardianId,


        java.util.Map<String, String> variableValues,


        Boolean hasWitnesses,


        List<Long> witnessUserIds
) {}
