package com.app.gerencia.controllers.dto.contract;

public record SignContractRequestDTO(
        Boolean acceptedTerms,

        java.util.Map<Long, String> acceptResponses
) {}
