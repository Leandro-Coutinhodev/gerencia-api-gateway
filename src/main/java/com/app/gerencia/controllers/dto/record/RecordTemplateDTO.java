package com.app.gerencia.controllers.dto.record;

import com.app.gerencia.entities.RecordTemplate;
import com.app.gerencia.entities.RecordTemplateActivity;
import com.app.gerencia.entities.RecordTemplateActivityField;
import com.app.gerencia.entities.RecordTemplateGeneralField;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public record RecordTemplateDTO(
        Long id,
        Long professionalId,
        String professionalName,
        String name,
        String description,
        boolean active,
        LocalDateTime createdAt,
        List<GeneralFieldDTO> generalFields,
        List<ActivityItemDTO> activityItems
) {
    public record GeneralFieldDTO(
            Long id, String label, String fieldType, String unit,
            boolean required, int position, List<String> options
    ) {
        static GeneralFieldDTO fromEntity(RecordTemplateGeneralField f) {
            return new GeneralFieldDTO(
                    f.getId(), f.getLabel(), f.getFieldType().name(), f.getUnit(),
                    f.isRequired(), f.getPosition() != null ? f.getPosition() : 0,
                    splitOptions(f.getOptions())
            );
        }
    }

    public record FieldDTO(
            Long id, String label, String fieldType, String unit,
            boolean required, int position, List<String> options
    ) {
        static FieldDTO fromEntity(RecordTemplateActivityField f) {
            return new FieldDTO(
                    f.getId(), f.getLabel(), f.getFieldType().name(), f.getUnit(),
                    f.isRequired(), f.getPosition() != null ? f.getPosition() : 0,
                    splitOptions(f.getOptions())
            );
        }
    }

    public record ActivityItemDTO(
            Long id, Long activityId, String activityName, String label,
            int position, List<FieldDTO> fields
    ) {
        static ActivityItemDTO fromEntity(RecordTemplateActivity item) {
            String label = item.getLabel() != null && !item.getLabel().isBlank()
                    ? item.getLabel() : item.getActivity().getName();
            return new ActivityItemDTO(
                    item.getId(),
                    item.getActivity().getId(),
                    item.getActivity().getName(),
                    label,
                    item.getPosition() != null ? item.getPosition() : 0,
                    item.getFields().stream().map(FieldDTO::fromEntity).toList()
            );
        }
    }

    private static List<String> splitOptions(String options) {
        return options != null && !options.isBlank()
                ? Arrays.asList(options.split("\\|")) : List.of();
    }

    public static RecordTemplateDTO fromEntity(RecordTemplate t) {
        return new RecordTemplateDTO(
                t.getId(),
                t.getProfessional() != null ? t.getProfessional().getId() : null,
                t.getProfessional() != null ? t.getProfessional().getName() : null,
                t.getName(),
                t.getDescription(),
                t.isActive(),
                t.getCreatedAt(),
                t.getGeneralFields().stream().map(GeneralFieldDTO::fromEntity).toList(),
                t.getActivityItems().stream().map(ActivityItemDTO::fromEntity).toList()
        );
    }
}
