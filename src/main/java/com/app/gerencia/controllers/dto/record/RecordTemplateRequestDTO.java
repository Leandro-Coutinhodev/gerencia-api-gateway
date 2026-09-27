package com.app.gerencia.controllers.dto.record;

import java.util.List;

public record RecordTemplateRequestDTO(
        String name,
        String description,
        List<GeneralFieldRequestDTO> generalFields,
        List<ActivityItemRequestDTO> activityItems
) {
    public record GeneralFieldRequestDTO(
            Long id,
            String label,
            String fieldType,
            String unit,
            boolean required,
            int position,
            String options
    ) {}

    public record ActivityItemRequestDTO(
            Long id,
            Long activityId,
            String label,
            int position,
            List<FieldRequestDTO> fields
    ) {}

    public record FieldRequestDTO(
            Long id,
            String label,
            String fieldType,
            String unit,
            boolean required,
            int position,
            String options
    ) {}
}
