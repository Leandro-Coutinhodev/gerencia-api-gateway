package com.app.gerencia.controllers.dto.contract;

public record CreateExternalContractRequestDTO(
        Long patientId,
        Long guardianId

) {}
