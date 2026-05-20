package com.app.gerencia.controllers.dto;

import com.app.gerencia.controllers.dto.AnamnesisTemplateDTO;
import com.app.gerencia.entities.Anamnesis;
import com.app.gerencia.entities.AnamnesisAnswer;
import com.app.gerencia.entities.AnamnesisTemplateField;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public record AnamnesisDTO(
        Long id,
        Character status,
        String interviewDate,
        Long patientId,
        String patientName,
        AnamnesisTemplateDTO template,
        List<AnswerDTO> answers,
        String formLink
) {

    // =========================================================
    // Construtor simples — findById, response
    // =========================================================

    public AnamnesisDTO(Anamnesis anamnesis) {
        this(
                anamnesis.getId(),
                anamnesis.getStatus(),
                anamnesis.getInterviewDate() != null
                        ? new SimpleDateFormat("yyyy-MM-dd").format(anamnesis.getInterviewDate())
                        : null,
                anamnesis.getPatient().getId(),
                anamnesis.getPatient().getName(),
                AnamnesisTemplateDTO.fromEntity(anamnesis.getTemplate()),
                mapAnswers(anamnesis.getId(), anamnesis.getAnswers()),
                null
        );
    }

    // =========================================================
    // Construtor com token — findByPatient, findAll, generateLink
    // =========================================================

    public AnamnesisDTO(Anamnesis anamnesis, String token, String host) {
        this(
                anamnesis.getId(),
                anamnesis.getStatus(),
                anamnesis.getInterviewDate() != null
                        ? new SimpleDateFormat("yyyy-MM-dd").format(anamnesis.getInterviewDate())
                        : null,
                anamnesis.getPatient().getId(),
                anamnesis.getPatient().getName(),
                AnamnesisTemplateDTO.fromEntity(anamnesis.getTemplate()),
                mapAnswers(anamnesis.getId(), anamnesis.getAnswers()),
                host + "/formulario?token=" + token
        );
    }

    // =========================================================
    // Helper privado
    // =========================================================

    private static List<AnswerDTO> mapAnswers(Long anamnesisId, List<AnamnesisAnswer> answers) {
        if (answers == null || answers.isEmpty()) return Collections.emptyList();

        String baseFileUrl = "/api-gateway/gerencia/anamnesis/" + anamnesisId + "/field";

        return answers.stream()
                .sorted(Comparator.comparingInt(a -> a.getField().getPosition()))
                .map(a -> AnswerDTO.fromEntity(a, baseFileUrl))
                .toList();
    }

    // =========================================================
    // AnswerDTO interno
    // =========================================================

    public record AnswerDTO(
            Long fieldId,
            String fieldLabel,
            String fieldType,
            String value,
            boolean hasFile,
            String fileName,
            String fileUrl
    ) {
        public static AnswerDTO fromEntity(AnamnesisAnswer answer, String baseFileUrl) {
            boolean isFile = answer.getField().getFieldType() == AnamnesisTemplateField.FieldType.FILE;
            boolean hasFile = isFile && answer.getFileData() != null;

            return new AnswerDTO(
                    answer.getField().getId(),
                    answer.getField().getLabel(),
                    answer.getField().getFieldType().name(),
                    isFile ? null : answer.getValue(),
                    hasFile,
                    isFile ? answer.getFileName() : null,
                    hasFile ? baseFileUrl + "/" + answer.getField().getId() + "/file" : null
            );
        }
    }
}