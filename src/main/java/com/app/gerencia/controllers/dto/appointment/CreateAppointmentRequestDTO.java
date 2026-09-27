package com.app.gerencia.controllers.dto.appointment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record CreateAppointmentRequestDTO(
        Long attendanceRecordId,   // ficha de atendimento de origem (RB02/RB03)
        String type,               // "UNICO" | "RECORRENTE"
        LocalTime time,
        Integer durationMinutes,
        String notes,

        // Obrigatório quando type = UNICO
        LocalDate date,

        // Obrigatório quando type = RECORRENTE
        RecurrenceRequestDTO recurrence
) {
    public record RecurrenceRequestDTO(
            String frequencyType,       // "DIARIA" | "SEMANAL" | "MENSAL"
            List<Integer> daysOfWeek,   // ISO: segunda=1 ... domingo=7 — obrigatório apenas para SEMANAL
            LocalDate startDate,
            LocalDate endDate
    ) {}
}
