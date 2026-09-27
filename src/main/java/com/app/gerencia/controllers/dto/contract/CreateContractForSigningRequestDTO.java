package com.app.gerencia.controllers.dto.contract;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreateContractForSigningRequestDTO(
        Long templateId,
        Long patientId,
        Long guardianId,


        java.util.Map<String, String> variableValues,


        Boolean hasWitnesses,


        List<Long> witnessUserIds,

        // Dados financeiros — obrigatórios, usados para controle financeiro e relatórios
        BigDecimal contractValue,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate paymentDate
) {}
