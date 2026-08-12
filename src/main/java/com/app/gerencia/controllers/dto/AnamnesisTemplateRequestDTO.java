package com.app.gerencia.controllers.dto;

import java.util.List;

public record AnamnesisTemplateRequestDTO(
        String name,
        String description,
        List<FieldRequestDTO> fields
) {
    public record FieldRequestDTO(
            Long id,
            String label,
            String fieldType,
            boolean required,
            int position,
            String placeholder,
            String options
    ) {}
}
