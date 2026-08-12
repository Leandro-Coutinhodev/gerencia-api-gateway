package com.app.gerencia.controllers.dto.contract;

import java.util.List;

public record CreateContractTemplateRequestDTO(
        String name,
        String description,
        String type,
        String signingMode,
        String witnessConfig,
        Integer witnessCount,
        List<ClauseRequest> clauses,
        List<VariableRequest> variables,
        List<AcceptFieldRequest> acceptFields
) {
    public record ClauseRequest(Integer clauseOrder, String title, String content) {}
    public record VariableRequest(String variableName, String description,
                                  String type, Boolean required, Boolean autoFilled) {}
    public record AcceptFieldRequest(Integer fieldOrder, String label,
                                     String fieldType, Boolean required) {}
}
