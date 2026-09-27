package com.app.gerencia.services;

import com.app.gerencia.controllers.dto.DashboardDTO;
import com.app.gerencia.controllers.dto.FinanceDashboardDTO;
import com.app.gerencia.entities.Anamnesis;
import com.app.gerencia.entities.AnamnesisReferral;
import com.app.gerencia.entities.Contract;
import com.app.gerencia.entities.Guardian;
import com.app.gerencia.entities.Patient;
import com.app.gerencia.entities.Professional;
import com.app.gerencia.enums.ContractStatus;
import com.app.gerencia.repository.AnamnesisReferralRepository;
import com.app.gerencia.repository.ContractRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTests {

    @Mock
    private FinanceDashboardService financeDashboardService;

    @Mock
    private PatientService patientService;

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private AnamnesisReferralRepository anamnesisReferralRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private Patient patient;

    @BeforeEach
    void setUp() {
        Guardian guardian = new Guardian();
        guardian.setName("Maria da Silva");

        patient = new Patient();
        patient.setId(1L);
        patient.setName("João da Silva");
        patient.setGuardian(guardian);
    }

    @Test
    void generate_cenarioFeliz_retornaOs4BlocosPreenchidos() {
        when(financeDashboardService.generate(null)).thenReturn(financeDashboardDTO());
        when(contractRepository.findByStatusOrderByCreatedAtDesc(ContractStatus.AGUARDANDO_ASSINATURA))
                .thenReturn(List.of(buildContract(1L)));
        when(patientService.findByBirthMonth(null)).thenReturn(List.of(patient));
        when(anamnesisReferralRepository.findByAnamnesis_StatusInOrderBySentAtDesc(anyList()))
                .thenReturn(List.of(buildReferral(1L)));

        DashboardDTO result = dashboardService.generate();

        assertThat(result.financeiro()).isNotNull();
        assertThat(result.financeiro().saldoAcumulado()).isEqualByComparingTo("1000.00");
        assertThat(result.financeiro().statusPagoContagem()).isEqualTo(3);
        assertThat(result.financeiro().statusPendenteContagem()).isEqualTo(2);
        assertThat(result.financeiro().statusAtrasadoContagem()).isEqualTo(1);
        assertThat(result.contratos()).isNotNull();
        assertThat(result.contratos().aguardandoAssinaturaContagem()).isEqualTo(1);
        assertThat(result.contratos().recentes()).hasSize(1);
        assertThat(result.aniversariantes()).containsExactly(patient);
        assertThat(result.anamnese()).isNotNull();
        assertThat(result.anamnese().pendentesContagem()).isEqualTo(1);
        assertThat(result.anamnese().recentes()).hasSize(1);
    }

    @Test
    void generate_blocoFinanceiroFalha_retornaNullSoNesseBloco() {
        when(financeDashboardService.generate(null)).thenThrow(new RuntimeException("boom"));
        when(contractRepository.findByStatusOrderByCreatedAtDesc(any())).thenReturn(List.of());
        when(patientService.findByBirthMonth(null)).thenReturn(List.of());
        when(anamnesisReferralRepository.findByAnamnesis_StatusInOrderBySentAtDesc(anyList())).thenReturn(List.of());

        DashboardDTO result = dashboardService.generate();

        assertThat(result.financeiro()).isNull();
        assertThat(result.contratos()).isNotNull();
        assertThat(result.aniversariantes()).isNotNull();
        assertThat(result.anamnese()).isNotNull();
    }

    @Test
    void generate_blocoContratosFalha_retornaNullSoNesseBloco() {
        when(financeDashboardService.generate(null)).thenReturn(financeDashboardDTO());
        when(contractRepository.findByStatusOrderByCreatedAtDesc(any())).thenThrow(new RuntimeException("boom"));
        when(patientService.findByBirthMonth(null)).thenReturn(List.of());
        when(anamnesisReferralRepository.findByAnamnesis_StatusInOrderBySentAtDesc(anyList())).thenReturn(List.of());

        DashboardDTO result = dashboardService.generate();

        assertThat(result.financeiro()).isNotNull();
        assertThat(result.contratos()).isNull();
        assertThat(result.aniversariantes()).isNotNull();
        assertThat(result.anamnese()).isNotNull();
    }

    @Test
    void generate_blocoAniversariantesFalha_retornaNullSoNesseBloco() {
        when(financeDashboardService.generate(null)).thenReturn(financeDashboardDTO());
        when(contractRepository.findByStatusOrderByCreatedAtDesc(any())).thenReturn(List.of());
        when(patientService.findByBirthMonth(null)).thenThrow(new RuntimeException("boom"));
        when(anamnesisReferralRepository.findByAnamnesis_StatusInOrderBySentAtDesc(anyList())).thenReturn(List.of());

        DashboardDTO result = dashboardService.generate();

        assertThat(result.financeiro()).isNotNull();
        assertThat(result.contratos()).isNotNull();
        assertThat(result.aniversariantes()).isNull();
        assertThat(result.anamnese()).isNotNull();
    }

    @Test
    void generate_blocoAnamneseFalha_retornaNullSoNesseBloco() {
        when(financeDashboardService.generate(null)).thenReturn(financeDashboardDTO());
        when(contractRepository.findByStatusOrderByCreatedAtDesc(any())).thenReturn(List.of());
        when(patientService.findByBirthMonth(null)).thenReturn(List.of());
        when(anamnesisReferralRepository.findByAnamnesis_StatusInOrderBySentAtDesc(anyList()))
                .thenThrow(new RuntimeException("boom"));

        DashboardDTO result = dashboardService.generate();

        assertThat(result.financeiro()).isNotNull();
        assertThat(result.contratos()).isNotNull();
        assertThat(result.aniversariantes()).isNotNull();
        assertThat(result.anamnese()).isNull();
    }

    @Test
    void generate_blocosSemDado_retornamZeroEListaVaziaNaoNull() {
        when(financeDashboardService.generate(null)).thenReturn(financeDashboardDTO());
        when(contractRepository.findByStatusOrderByCreatedAtDesc(any())).thenReturn(List.of());
        when(patientService.findByBirthMonth(null)).thenReturn(List.of());
        when(anamnesisReferralRepository.findByAnamnesis_StatusInOrderBySentAtDesc(anyList())).thenReturn(List.of());

        DashboardDTO result = dashboardService.generate();

        assertThat(result.contratos()).isNotNull();
        assertThat(result.contratos().aguardandoAssinaturaContagem()).isZero();
        assertThat(result.contratos().recentes()).isEmpty();
        assertThat(result.aniversariantes()).isEmpty();
        assertThat(result.anamnese()).isNotNull();
        assertThat(result.anamnese().pendentesContagem()).isZero();
        assertThat(result.anamnese().recentes()).isEmpty();
    }

    private FinanceDashboardDTO financeDashboardDTO() {
        return new FinanceDashboardDTO(
                "2026-09", new BigDecimal("400.00"), new BigDecimal("50.00"),
                new BigDecimal("1000.00"), new BigDecimal("10.00"),
                new BigDecimal("300.00"), 2, 3, 2, 1, List.of()
        );
    }

    private Contract buildContract(Long id) {
        Contract contract = new Contract();
        contract.setId(id);
        contract.setPatient(patient);
        contract.setStatus(ContractStatus.AGUARDANDO_ASSINATURA);
        return contract;
    }

    private AnamnesisReferral buildReferral(Long id) {
        Anamnesis anamnesis = new Anamnesis();
        anamnesis.setId(id);
        anamnesis.setPatient(patient);
        anamnesis.setStatus('E');

        Professional professional = new Professional();
        professional.setId(1L);
        professional.setName("Dr. Profissional Teste");

        AnamnesisReferral referral = new AnamnesisReferral();
        referral.setId(id);
        referral.setAnamnesis(anamnesis);
        referral.setProfessional(professional);
        referral.setSentAt(new Date());
        return referral;
    }
}
