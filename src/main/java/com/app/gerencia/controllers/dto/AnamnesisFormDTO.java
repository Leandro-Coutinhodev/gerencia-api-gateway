package com.app.gerencia.controllers.dto;

import com.app.gerencia.entities.Anamnesis;
import com.app.gerencia.entities.AnamnesisAnswer;
import com.app.gerencia.entities.AnamnesisTemplateField;

import java.util.List;

public class AnamnesisFormDTO {

    private Long anamnesisId;
    private Character status;
    private String patientName;
    private String formLink;

    private AnamnesisTemplateDTO template;
    private List<AnswerDTO> existingAnswers;

    // =========================================================
    // Construtor principal — usado no getFormData
    // =========================================================

    public AnamnesisFormDTO(Anamnesis anamnesis,
                            List<AnamnesisAnswer> existingAnswers,
                            String token,
                            String host) {
        this.anamnesisId   = anamnesis.getId();
        this.status        = anamnesis.getStatus();
        this.patientName   = anamnesis.getPatient().getName();
        this.formLink      = host + "/formulario?token=" + token;
        this.template      = AnamnesisTemplateDTO.fromEntity(anamnesis.getTemplate());
        this.existingAnswers = existingAnswers.stream()
                .map(AnswerDTO::fromEntity)
                .toList();
    }

    // =========================================================
    // AnswerDTO — respostas já salvas, indexadas por fieldId
    // =========================================================

    public static class AnswerDTO {

        private Long fieldId;
        private String fieldType; // TEXT, TEXTAREA, DATE, CHECKBOX, FILE
        private String value;     // null para FILE
        private String fileName;  // preenchido apenas para FILE
        private boolean hasFile;  // sinaliza ao frontend que existe arquivo salvo

        public static AnswerDTO fromEntity(AnamnesisAnswer answer) {
            AnswerDTO dto = new AnswerDTO();
            dto.fieldId   = answer.getField().getId();
            dto.fieldType = answer.getField().getFieldType().name();

            if (answer.getField().getFieldType() == AnamnesisTemplateField.FieldType.FILE) {
                dto.hasFile  = answer.getFileData() != null;
                dto.fileName = answer.getFileName();
                dto.value    = null;
            } else {
                dto.value   = answer.getValue();
                dto.hasFile = false;
            }

            return dto;
        }

        // Getters
        public Long getFieldId()     { return fieldId; }
        public String getFieldType() { return fieldType; }
        public String getValue()     { return value; }
        public String getFileName()  { return fileName; }
        public boolean isHasFile()   { return hasFile; }
    }

    // =========================================================
    // Getters
    // =========================================================

    public Long getAnamnesisId()                  { return anamnesisId; }
    public Character getStatus()                  { return status; }
    public String getPatientName()                { return patientName; }
    public String getFormLink()                   { return formLink; }
    public AnamnesisTemplateDTO getTemplate()     { return template; }
    public List<AnswerDTO> getExistingAnswers()   { return existingAnswers; }
}
