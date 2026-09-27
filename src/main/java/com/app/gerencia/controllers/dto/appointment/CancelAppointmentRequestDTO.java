package com.app.gerencia.controllers.dto.appointment;

public record CancelAppointmentRequestDTO(
        String scope,       // THIS | FROM_HERE | ALL
        Long frequencyId    // ocorrência de referência — obrigatório para THIS e FROM_HERE
) {}
