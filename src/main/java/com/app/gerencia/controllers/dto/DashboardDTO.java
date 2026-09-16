package com.app.gerencia.controllers.dto;

import com.app.gerencia.controllers.dto.contract.ContractDTO;
import com.app.gerencia.entities.Patient;

import java.math.BigDecimal;
import java.util.List;

public record DashboardDTO(
        FinanceiroResumoDTO financeiro,
        ContratosResumoDTO contratos,
        List<Patient> aniversariantes,
        AnamneseResumoDTO anamnese
) {
    public record FinanceiroResumoDTO(
            BigDecimal saldoAcumulado,
            BigDecimal aReceberValor,
            long statusPagoContagem,
            long statusPendenteContagem,
            long statusAtrasadoContagem
    ) {
    }

    public record ContratosResumoDTO(
            long aguardandoAssinaturaContagem,
            List<ContractDTO> recentes
    ) {
    }

    public record AnamneseResumoDTO(
            long pendentesContagem,
            List<AnamnesisReferralResponseDTO> recentes
    ) {
    }
}
