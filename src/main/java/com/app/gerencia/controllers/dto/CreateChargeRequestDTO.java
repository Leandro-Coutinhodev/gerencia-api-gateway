package com.app.gerencia.controllers.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateChargeRequestDTO(
        Long patientId,
        BigDecimal valor,
        BigDecimal desconto,
        String formaPagamento,
        LocalDate vencimento
) {
}
