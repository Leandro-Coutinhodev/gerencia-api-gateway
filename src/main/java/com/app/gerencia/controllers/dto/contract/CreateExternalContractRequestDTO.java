package com.app.gerencia.controllers.dto.contract;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateExternalContractRequestDTO(
        Long patientId,
        Long guardianId,

        // Dados financeiros — obrigatórios, usados para controle financeiro e relatórios
        BigDecimal contractValue,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate paymentDate
) {}
