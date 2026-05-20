package com.app.gerencia.controllers.dto;

import java.util.List;

public record AnamnesisResponseSubmitDTO(
        List<AnswerDTO> answers
) {
    public record AnswerDTO(
            Long fieldId,
            String value  // null se for FILE (vem via multipart separado)
    ) {}
}