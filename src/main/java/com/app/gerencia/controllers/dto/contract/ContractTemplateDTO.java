package com.app.gerencia.controllers.dto.contract;

import com.app.gerencia.entities.*;
import com.app.gerencia.enums.ContractStatus;
import com.app.gerencia.enums.ParticipantRole;

import java.time.LocalDateTime;
import java.util.List;

public record ContractTemplateDTO(
        Long id,
        String name,
        String description,
        ContractTemplate.ContractType type,
        ContractTemplate.ContractTemplateStatus status,
        ContractTemplate.SigningMode signingMode,
        ContractTemplate.WitnessConfig witnessConfig,
        Integer witnessCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ClauseDTO> clauses,
        List<VariableDTO> variables,
        List<AcceptFieldDTO> acceptFields
) {
    public static ContractTemplateDTO fromEntity(ContractTemplate t) {
        return new ContractTemplateDTO(
                t.getId(), t.getName(), t.getDescription(),
                t.getType(), t.getStatus(), t.getSigningMode(),
                t.getWitnessConfig(), t.getWitnessCount(),
                t.getCreatedAt(), t.getUpdatedAt(),
                t.getClauses() == null ? List.of() :
                        t.getClauses().stream().map(ClauseDTO::fromEntity).toList(),
                t.getVariables() == null ? List.of() :
                        t.getVariables().stream().map(VariableDTO::fromEntity).toList(),
                t.getAcceptFields() == null ? List.of() :
                        t.getAcceptFields().stream().map(AcceptFieldDTO::fromEntity).toList()
        );
    }

    public record ClauseDTO(
            Long id, Integer clauseOrder, String title, String content
    ) {
        public static ClauseDTO fromEntity(ContractClause c) {
            return new ClauseDTO(c.getId(), c.getClauseOrder(), c.getTitle(), c.getContent());
        }
    }

    public record VariableDTO(
            Long id, String variableName, String description,
            ContractVariable.VariableType type, Boolean required, Boolean autoFilled
    ) {
        public static VariableDTO fromEntity(ContractVariable v) {
            return new VariableDTO(v.getId(), v.getVariableName(), v.getDescription(),
                    v.getType(), v.getRequired(), v.getAutoFilled());
        }
    }

    public record AcceptFieldDTO(
            Long id, Integer fieldOrder, String label,
            ContractAcceptField.AcceptFieldType fieldType, Boolean required
    ) {
        public static AcceptFieldDTO fromEntity(ContractAcceptField f) {
            return new AcceptFieldDTO(f.getId(), f.getFieldOrder(), f.getLabel(),
                    f.getFieldType(), f.getRequired());
        }
    }
}



