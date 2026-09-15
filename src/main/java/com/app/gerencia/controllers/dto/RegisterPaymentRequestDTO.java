package com.app.gerencia.controllers.dto;

import java.time.LocalDate;

public record RegisterPaymentRequestDTO(
        String formaPagamento,
        LocalDate dataPagamento
) {
}
