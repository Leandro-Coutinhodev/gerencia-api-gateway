package com.app.gerencia.controllers.dto.record;

import com.app.gerencia.entities.AttendanceRecord;
import com.app.gerencia.entities.RecordTemplateActivity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record RecordDTO(
        Long id,
        Long templateId,
        String templateName,
        Long patientId,
        String patientName,
        Long professionalId,
        String professionalName,
        LocalDate sessionDate,
        LocalDateTime createdAt,
        List<GeneralAnswerDTO> generalAnswers,
        List<ActivityAnswerDTO> activityAnswers
) {
    public record GeneralAnswerDTO(
            Long fieldId, String label, String fieldType, String unit, String value
    ) {}

    public record ActivityFieldAnswerDTO(
            Long fieldId, String label, String fieldType, String unit, String value
    ) {}

    public record ActivityAnswerDTO(
            Long templateActivityId, String activityName, String label,
            List<ActivityFieldAnswerDTO> fields
    ) {}

    // `answers` já deve ter sido extraído de record.getAnswersData() (RecordService.parseAnswers)
    public static RecordDTO fromEntity(AttendanceRecord r, RecordAnswers answers) {
        var template = r.getTemplate();

        List<GeneralAnswerDTO> generalDTOs = template.getGeneralFields().stream()
                .map(f -> new GeneralAnswerDTO(
                        f.getId(), f.getLabel(), f.getFieldType().name(), f.getUnit(),
                        answers.getGeneral().get(String.valueOf(f.getId()))
                ))
                .toList();

        List<ActivityAnswerDTO> activityDTOs = template.getActivityItems().stream()
                .map(item -> toActivityAnswerDTO(item, answers))
                .toList();

        return new RecordDTO(
                r.getId(),
                template.getId(), template.getName(),
                r.getPatient().getId(), r.getPatient().getName(),
                r.getProfessional().getId(), r.getProfessional().getName(),
                r.getSessionDate(), r.getCreatedAt(),
                generalDTOs, activityDTOs
        );
    }

    private static ActivityAnswerDTO toActivityAnswerDTO(RecordTemplateActivity item, RecordAnswers answers) {
        Map<String, String> itemAnswers = answers.getActivities()
                .getOrDefault(String.valueOf(item.getId()), Map.of());

        List<ActivityFieldAnswerDTO> fieldDTOs = item.getFields().stream()
                .map(f -> new ActivityFieldAnswerDTO(
                        f.getId(), f.getLabel(), f.getFieldType().name(), f.getUnit(),
                        itemAnswers.get(String.valueOf(f.getId()))
                ))
                .toList();

        String label = item.getLabel() != null && !item.getLabel().isBlank()
                ? item.getLabel() : item.getActivity().getName();

        return new ActivityAnswerDTO(item.getId(), item.getActivity().getName(), label, fieldDTOs);
    }
}
