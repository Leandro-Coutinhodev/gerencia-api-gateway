package com.app.gerencia.controllers.dto.record;

import java.util.LinkedHashMap;
import java.util.Map;

// Formato persistido em Record.answersData (serializado via Gson).
// general:    fieldId (RecordTemplateGeneralField) -> valor
// activities: templateActivityId (RecordTemplateActivity) -> (fieldId (RecordTemplateActivityField) -> valor)
public class RecordAnswers {

    private Map<String, String> general = new LinkedHashMap<>();
    private Map<String, Map<String, String>> activities = new LinkedHashMap<>();

    public Map<String, String> getGeneral() { return general; }
    public void setGeneral(Map<String, String> general) { this.general = general; }

    public Map<String, Map<String, String>> getActivities() { return activities; }
    public void setActivities(Map<String, Map<String, String>> activities) { this.activities = activities; }
}
