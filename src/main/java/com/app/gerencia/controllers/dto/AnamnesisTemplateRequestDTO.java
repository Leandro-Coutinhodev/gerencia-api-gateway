package com.app.gerencia.controllers.dto;

import java.util.List;

public record AnamnesisTemplateRequestDTO(
        String name,
        String description,
        List<FieldRequestDTO> fields
) {
    public record FieldRequestDTO(
            Long id,          // null se novo, preenchido se edição
            String label,
            String fieldType, // "TEXT", "TEXTAREA", "DATE", "CHECKBOX", "FILE"
            boolean required,
            int position,
            String placeholder,
            String options    // Para checkbox: "Sim|Não"
    ) {}
}
