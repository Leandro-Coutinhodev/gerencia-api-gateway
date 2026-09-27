package com.app.gerencia.services;

import com.app.gerencia.controllers.dto.AnamnesisReferralResponseDTO;
import com.app.gerencia.controllers.dto.DashboardDTO;
import com.app.gerencia.controllers.dto.FinanceDashboardDTO;
import com.app.gerencia.controllers.dto.contract.ContractDTO;
import com.app.gerencia.entities.Patient;
import com.app.gerencia.enums.ContractStatus;
import com.app.gerencia.repository.AnamnesisReferralRepository;
import com.app.gerencia.repository.ContractRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);
    private static final int RECENTES_LIMITE = 5;
    private static final List<Character> ANAMNESE_STATUS_PENDENTE = List.of('E', 'P');

    private final FinanceDashboardService financeDashboardService;
    private final PatientService patientService;
    private final ContractRepository contractRepository;
    private final AnamnesisReferralRepository anamnesisReferralRepository;

    public DashboardService(FinanceDashboardService financeDashboardService,
                             PatientService patientService,
                             ContractRepository contractRepository,
                             AnamnesisReferralRepository anamnesisReferralRepository) {
        this.financeDashboardService = financeDashboardService;
        this.patientService = patientService;
        this.contractRepository = contractRepository;
        this.anamnesisReferralRepository = anamnesisReferralRepository;
    }

    public DashboardDTO generate() {
        return new DashboardDTO(
                buildFinanceiro(),
                buildContratos(),
                buildAniversariantes(),
                buildAnamnese()
        );
    }

    private DashboardDTO.FinanceiroResumoDTO buildFinanceiro() {
        try {
            FinanceDashboardDTO financeiro = financeDashboardService.generate(null);
            return new DashboardDTO.FinanceiroResumoDTO(
                    financeiro.saldoAcumulado(),
                    financeiro.aReceberValor(),
                    financeiro.statusPagoContagem(),
                    financeiro.statusPendenteContagem(),
                    financeiro.statusAtrasadoContagem()
            );
        } catch (Exception e) {
            log.warn("Falha ao calcular o bloco financeiro do dashboard geral", e);
            return null;
        }
    }

    private DashboardDTO.ContratosResumoDTO buildContratos() {
        try {
            List<ContractDTO> aguardando = contractRepository
                    .findByStatusOrderByCreatedAtDesc(ContractStatus.AGUARDANDO_ASSINATURA)
                    .stream()
                    .map(ContractDTO::fromEntity)
                    .toList();
            List<ContractDTO> recentes = aguardando.stream().limit(RECENTES_LIMITE).toList();
            return new DashboardDTO.ContratosResumoDTO(aguardando.size(), recentes);
        } catch (Exception e) {
            log.warn("Falha ao calcular o bloco de contratos do dashboard geral", e);
            return null;
        }
    }

    private List<Patient> buildAniversariantes() {
        try {
            return patientService.findByBirthMonth(null);
        } catch (Exception e) {
            log.warn("Falha ao calcular o bloco de aniversariantes do dashboard geral", e);
            return null;
        }
    }

    private DashboardDTO.AnamneseResumoDTO buildAnamnese() {
        try {
            var pendentes = anamnesisReferralRepository
                    .findByAnamnesis_StatusInOrderBySentAtDesc(ANAMNESE_STATUS_PENDENTE);
            List<AnamnesisReferralResponseDTO> recentes = pendentes.stream()
                    .limit(RECENTES_LIMITE)
                    .map(AnamnesisReferralResponseDTO::fromEntity)
                    .toList();
            return new DashboardDTO.AnamneseResumoDTO(pendentes.size(), recentes);
        } catch (Exception e) {
            log.warn("Falha ao calcular o bloco de anamnese do dashboard geral", e);
            return null;
        }
    }
}
