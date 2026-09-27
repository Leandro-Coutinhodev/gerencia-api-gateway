package com.app.gerencia.controllers.dto;

import java.math.BigDecimal;
import java.util.List;

public record FinanceDashboardDTO(
        String periodo,
        BigDecimal entradas,
        BigDecimal saidas,
        BigDecimal saldoAcumulado,
        BigDecimal variacaoPercentual,
        BigDecimal aReceberValor,
        long aReceberContagem,
        long statusPagoContagem,
        long statusPendenteContagem,
        long statusAtrasadoContagem,
        List<ChargeDTO> mensalidadesRecentes
) {
}
