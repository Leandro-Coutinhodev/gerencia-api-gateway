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



    public static class AnswerDTO {

        private Long fieldId;
        private String fieldType;
        private String value;
        private String fileName;
        private boolean hasFile;

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


        public Long getFieldId()     { return fieldId; }
        public String getFieldType() { return fieldType; }
        public String getValue()     { return value; }
        public String getFileName()  { return fileName; }
        public boolean isHasFile()   { return hasFile; }
    }



    public Long getAnamnesisId()                  { return anamnesisId; }
    public Character getStatus()                  { return status; }
    public String getPatientName()                { return patientName; }
    public String getFormLink()                   { return formLink; }
    public AnamnesisTemplateDTO getTemplate()     { return template; }
    public List<AnswerDTO> getExistingAnswers()   { return existingAnswers; }
}
