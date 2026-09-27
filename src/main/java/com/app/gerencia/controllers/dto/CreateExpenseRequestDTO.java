package com.app.gerencia.controllers.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateExpenseRequestDTO(
        BigDecimal valor,
        String categoria,
        LocalDate data
) {
}
