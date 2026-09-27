package com.app.gerencia.controllers.dto.record;

import java.time.LocalDate;
import java.util.Map;

public record CreateRecordRequestDTO(
        Long templateId,
        Long patientId,
        LocalDate sessionDate,

        // key = RecordTemplateGeneralField id (como string)
        Map<String, String> generalAnswers,

        // key = RecordTemplateActivity id (como string) -> (RecordTemplateActivityField id -> valor)
        Map<String, Map<String, String>> activityAnswers
) {}
