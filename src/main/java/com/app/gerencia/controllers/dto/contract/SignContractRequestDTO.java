package com.app.gerencia.controllers.dto.contract;

public record SignContractRequestDTO(
        Boolean acceptedTerms,
        /** Respostas aos campos de aceite: { fieldId -> valor } */
        java.util.Map<Long, String> acceptResponses
) {}
