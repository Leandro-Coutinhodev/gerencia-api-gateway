package com.app.gerencia.controllers.dto;

import com.app.gerencia.entities.AnamnesisTemplate;

import java.util.Arrays;
import java.util.List;

public record AnamnesisTemplateDTO(
        Long id,
        String name,
        String description,
        boolean active,
        List<FieldDTO> fields
) {
    public record FieldDTO(
            Long id,
            String label,
            String fieldType,
            boolean required,
            int position,
            String placeholder,
            List<String> options // já splitado por "|"
    ) {}

    public static AnamnesisTemplateDTO fromEntity(AnamnesisTemplate t) {
        return new AnamnesisTemplateDTO(
                t.getId(), t.getName(), t.getDescription(), t.isActive(),
                t.getFields().stream().map(f -> new FieldDTO(
                        f.getId(), f.getLabel(), f.getFieldType().name(),
                        f.isRequired(), f.getPosition(), f.getPlaceholder(),
                        f.getOptions() != null ? Arrays.asList(f.getOptions().split("\\|")) : List.of()
                )).toList()
        );
    }
}
