package com.app.gerencia.controllers.dto.appointment;

public record AppointmentSummaryDTO(
        long scheduled,
        long present,
        long absent,
        long justified,
        long cancelled
) {}
