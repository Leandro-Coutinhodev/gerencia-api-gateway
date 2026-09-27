package com.app.gerencia.controllers.dto;

import com.app.gerencia.entities.Expense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ExpenseDTO(
        Long id,
        BigDecimal valor,
        String categoria,
        LocalDate data,
        LocalDateTime createdAt
) {
    public static ExpenseDTO fromEntity(Expense e) {
        return new ExpenseDTO(e.getId(), e.getValor(), e.getCategoria(), e.getData(), e.getCreatedAt());
    }
}
