package com.app.gerencia.controllers.dto.appointment;

import com.app.gerencia.entities.Frequency;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record FrequencyDTO(
        Long id,
        Long appointmentId,
        String appointmentType,
        Long professionalId,
        String professionalName,
        Long patientId,
        String patientName,
        Long attendanceRecordId,
        String attendanceFormName,
        LocalDate scheduledDate,
        LocalTime scheduledTime,
        Integer durationMinutes,
        String status,
        String observation,
        LocalDateTime markedAt,
        LocalDateTime createdAt
) {
    public static FrequencyDTO fromEntity(Frequency f) {
        var template = f.getAttendanceRecord().getTemplate();
        return new FrequencyDTO(
                f.getId(),
                f.getAppointment().getId(),
                f.getAppointment().getType().name(),
                f.getProfessional().getId(),
                f.getProfessional().getName(),
                f.getPatient().getId(),
                f.getPatient().getName(),
                f.getAttendanceRecord().getId(),
                template != null ? template.getName() : null,
                f.getScheduledDate(),
                f.getScheduledTime(),
                f.getDurationMinutes(),
                f.getStatus().name(),
                f.getObservation(),
                f.getMarkedAt(),
                f.getCreatedAt()
        );
    }
}
