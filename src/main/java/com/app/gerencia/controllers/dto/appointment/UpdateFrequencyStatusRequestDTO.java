package com.app.gerencia.controllers.dto.appointment;

public record UpdateFrequencyStatusRequestDTO(
        String status,      // AGENDADO | PRESENTE | AUSENTE | JUSTIFICADO | CANCELADO
        String observation
) {}
