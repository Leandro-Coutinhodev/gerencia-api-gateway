package com.app.gerencia.controllers.dto;

import com.app.gerencia.entities.Charge;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record ChargeDTO(
        Long id,
        Long patientId,
        String patientName,
        String guardianName,
        BigDecimal valor,
        BigDecimal desconto,
        Charge.PaymentMethod formaPagamento,
        LocalDate vencimento,
        Charge.ChargeStatus status,
        LocalDate dataPagamento,
        LocalDateTime createdAt,
        boolean atrasado,
        int diasAtraso
) {
    public static ChargeDTO fromEntity(Charge c, int diasParaAtraso) {
        long diasDesdeVencimento = ChronoUnit.DAYS.between(c.getVencimento(), LocalDate.now());
        boolean atrasado = c.getStatus() == Charge.ChargeStatus.PENDENTE && diasDesdeVencimento > diasParaAtraso;
        int diasAtraso = atrasado ? (int) diasDesdeVencimento : 0;

        return new ChargeDTO(
                c.getId(),
                c.getPatient().getId(),
                c.getPatient().getName(),
                c.getPatient().getGuardian() != null ? c.getPatient().getGuardian().getName() : null,
                c.getValor(),
                c.getDesconto(),
                c.getFormaPagamento(),
                c.getVencimento(),
                c.getStatus(),
                c.getDataPagamento(),
                c.getCreatedAt(),
                atrasado,
                diasAtraso
        );
    }
}
