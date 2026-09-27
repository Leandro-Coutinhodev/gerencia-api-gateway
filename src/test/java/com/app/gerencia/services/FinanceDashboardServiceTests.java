package com.app.gerencia.services;

import com.app.gerencia.controllers.dto.FinanceDashboardDTO;
import com.app.gerencia.entities.Charge;
import com.app.gerencia.entities.Expense;
import com.app.gerencia.entities.Guardian;
import com.app.gerencia.entities.Patient;
import com.app.gerencia.repository.ChargeRepository;
import com.app.gerencia.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinanceDashboardServiceTests {

    @Mock
    private ChargeRepository chargeRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private FinanceSettingsService financeSettingsService;

    @InjectMocks
    private FinanceDashboardService financeDashboardService;

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
    void generate_mesEspecifico_calculaSaldoAcumuladoEVariacao() {
        Charge pagoAntes = buildCharge(1L, Charge.ChargeStatus.PAGO, LocalDate.of(2026, 7, 10));
        pagoAntes.setValor(new BigDecimal("1000.00"));
        pagoAntes.setDataPagamento(LocalDate.of(2026, 7, 15));

        Charge pagoNoMes = buildCharge(2L, Charge.ChargeStatus.PAGO, LocalDate.of(2026, 8, 10));
        pagoNoMes.setValor(new BigDecimal("500.00"));
        pagoNoMes.setDesconto(new BigDecimal("100.00"));
        pagoNoMes.setDataPagamento(LocalDate.of(2026, 8, 10));

        Expense despesaAntes = buildExpense(new BigDecimal("200.00"), LocalDate.of(2026, 7, 20));
        Expense despesaNoMes = buildExpense(new BigDecimal("50.00"), LocalDate.of(2026, 8, 5));

        when(chargeRepository.findByStatusOrderByVencimentoAsc(Charge.ChargeStatus.PAGO))
                .thenReturn(List.of(pagoAntes, pagoNoMes));
        when(expenseRepository.findAllByOrderByDataDesc()).thenReturn(List.of(despesaAntes, despesaNoMes));
        when(chargeRepository.findByStatusOrderByVencimentoAsc(Charge.ChargeStatus.PENDENTE)).thenReturn(List.of());
        when(chargeRepository.findByVencimentoBetween(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)))
                .thenReturn(List.of());
        when(financeSettingsService.getDiasParaAtrasoValue()).thenReturn(15);

        FinanceDashboardDTO result = financeDashboardService.generate("2026-08");

        assertThat(result.entradas()).isEqualByComparingTo("400.00");
        assertThat(result.saidas()).isEqualByComparingTo("50.00");
        assertThat(result.saldoAcumulado()).isEqualByComparingTo("1150.00");
        assertThat(result.variacaoPercentual()).isEqualByComparingTo("43.7500");
    }

    @Test
    void generate_saldoAnteriorZero_variacaoPercentualZero() {
        Charge pagoNoMes = buildCharge(1L, Charge.ChargeStatus.PAGO, LocalDate.of(2026, 8, 10));
        pagoNoMes.setValor(new BigDecimal("300.00"));
        pagoNoMes.setDataPagamento(LocalDate.of(2026, 8, 10));

        when(chargeRepository.findByStatusOrderByVencimentoAsc(Charge.ChargeStatus.PAGO)).thenReturn(List.of(pagoNoMes));
        when(expenseRepository.findAllByOrderByDataDesc()).thenReturn(List.of());
        when(chargeRepository.findByStatusOrderByVencimentoAsc(Charge.ChargeStatus.PENDENTE)).thenReturn(List.of());
        when(chargeRepository.findByVencimentoBetween(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)))
                .thenReturn(List.of());
        when(financeSettingsService.getDiasParaAtrasoValue()).thenReturn(15);

        FinanceDashboardDTO result = financeDashboardService.generate("2026-08");

        assertThat(result.variacaoPercentual()).isEqualByComparingTo("0");
    }

    @Test
    void generate_modoTudo_agregaSemCorteDeData() {
        Charge pagoJulho = buildCharge(1L, Charge.ChargeStatus.PAGO, LocalDate.of(2026, 7, 10));
        pagoJulho.setValor(new BigDecimal("1000.00"));
        pagoJulho.setDataPagamento(LocalDate.of(2026, 7, 15));

        Charge pagoAgosto = buildCharge(2L, Charge.ChargeStatus.PAGO, LocalDate.of(2026, 8, 10));
        pagoAgosto.setValor(new BigDecimal("500.00"));
        pagoAgosto.setDataPagamento(LocalDate.of(2026, 8, 10));

        Expense despesaJulho = buildExpense(new BigDecimal("200.00"), LocalDate.of(2026, 7, 20));

        when(chargeRepository.findByStatusOrderByVencimentoAsc(Charge.ChargeStatus.PAGO))
                .thenReturn(List.of(pagoJulho, pagoAgosto));
        when(expenseRepository.findAllByOrderByDataDesc()).thenReturn(List.of(despesaJulho));
        when(chargeRepository.findByStatusOrderByVencimentoAsc(Charge.ChargeStatus.PENDENTE)).thenReturn(List.of());
        when(chargeRepository.findAllByOrderByVencimentoAsc()).thenReturn(List.of(pagoJulho, pagoAgosto));
        when(financeSettingsService.getDiasParaAtrasoValue()).thenReturn(15);

        FinanceDashboardDTO result = financeDashboardService.generate("TUDO");

        assertThat(result.entradas()).isEqualByComparingTo("1500.00");
        assertThat(result.saidas()).isEqualByComparingTo("200.00");
        assertThat(result.saldoAcumulado()).isEqualByComparingTo("1300.00");
        // mês de referência da variação = agosto (mais recente com movimento)
        assertThat(result.variacaoPercentual()).isEqualByComparingTo("62.5000");
    }

    @Test
    void generate_aReceber_cumulativoAteFimDoMesIncluiAtrasoDeMesAnterior() {
        Charge atrasada = buildCharge(1L, Charge.ChargeStatus.PENDENTE, LocalDate.of(2026, 6, 1));
        atrasada.setValor(new BigDecimal("300.00"));
        Charge doMes = buildCharge(2L, Charge.ChargeStatus.PENDENTE, LocalDate.of(2026, 8, 20));
        doMes.setValor(new BigDecimal("200.00"));
        doMes.setDesconto(new BigDecimal("50.00"));
        Charge futura = buildCharge(3L, Charge.ChargeStatus.PENDENTE, LocalDate.of(2026, 9, 1));
        futura.setValor(new BigDecimal("999.00"));

        when(chargeRepository.findByStatusOrderByVencimentoAsc(Charge.ChargeStatus.PAGO)).thenReturn(List.of());
        when(expenseRepository.findAllByOrderByDataDesc()).thenReturn(List.of());
        when(chargeRepository.findByStatusOrderByVencimentoAsc(Charge.ChargeStatus.PENDENTE))
                .thenReturn(List.of(atrasada, doMes, futura));
        when(chargeRepository.findByVencimentoBetween(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)))
                .thenReturn(List.of());
        when(financeSettingsService.getDiasParaAtrasoValue()).thenReturn(15);

        FinanceDashboardDTO result = financeDashboardService.generate("2026-08");

        assertThat(result.aReceberContagem()).isEqualTo(2);
        assertThat(result.aReceberValor()).isEqualByComparingTo("450.00");
    }

    @Test
    void generate_resumoDeStatus_contaSoDoMesFiltrado() {
        // Datas relativas a "hoje" (não fixas) porque a classificação de atraso em
        // ChargeDTO.fromEntity usa LocalDate.now() — datas fixas quebrariam o teste
        // conforme o tempo passa.
        LocalDate hoje = LocalDate.now();
        YearMonth mesAtual = YearMonth.from(hoje);
        LocalDate inicioMes = mesAtual.atDay(1);
        LocalDate fimMes = mesAtual.atEndOfMonth();

        Charge pago = buildCharge(1L, Charge.ChargeStatus.PAGO, hoje);
        pago.setDataPagamento(hoje);
        Charge pendenteNoPrazo = buildCharge(2L, Charge.ChargeStatus.PENDENTE, hoje.minusDays(1));
        Charge pendenteAtrasada = buildCharge(3L, Charge.ChargeStatus.PENDENTE, hoje.minusDays(30));

        when(chargeRepository.findByStatusOrderByVencimentoAsc(Charge.ChargeStatus.PAGO)).thenReturn(List.of(pago));
        when(expenseRepository.findAllByOrderByDataDesc()).thenReturn(List.of());
        when(chargeRepository.findByStatusOrderByVencimentoAsc(Charge.ChargeStatus.PENDENTE))
                .thenReturn(List.of(pendenteNoPrazo, pendenteAtrasada));
        when(chargeRepository.findByVencimentoBetween(inicioMes, fimMes))
                .thenReturn(List.of(pago, pendenteNoPrazo, pendenteAtrasada));
        when(financeSettingsService.getDiasParaAtrasoValue()).thenReturn(15);

        FinanceDashboardDTO result = financeDashboardService.generate(mesAtual.toString());

        assertThat(result.statusPagoContagem()).isEqualTo(1);
        assertThat(result.statusPendenteContagem()).isEqualTo(1);
        assertThat(result.statusAtrasadoContagem()).isEqualTo(1);
    }

    @Test
    void generate_mensalidadesRecentes_limitaA5OrdenadoPorVencimentoDesc() {
        List<Charge> charges = List.of(
                buildCharge(1L, Charge.ChargeStatus.PENDENTE, LocalDate.of(2026, 8, 1)),
                buildCharge(2L, Charge.ChargeStatus.PENDENTE, LocalDate.of(2026, 8, 5)),
                buildCharge(3L, Charge.ChargeStatus.PENDENTE, LocalDate.of(2026, 8, 10)),
                buildCharge(4L, Charge.ChargeStatus.PENDENTE, LocalDate.of(2026, 8, 15)),
                buildCharge(5L, Charge.ChargeStatus.PENDENTE, LocalDate.of(2026, 8, 20)),
                buildCharge(6L, Charge.ChargeStatus.PENDENTE, LocalDate.of(2026, 8, 25)),
                buildCharge(7L, Charge.ChargeStatus.PENDENTE, LocalDate.of(2026, 8, 30))
        );

        when(chargeRepository.findByStatusOrderByVencimentoAsc(Charge.ChargeStatus.PAGO)).thenReturn(List.of());
        when(expenseRepository.findAllByOrderByDataDesc()).thenReturn(List.of());
        when(chargeRepository.findByStatusOrderByVencimentoAsc(Charge.ChargeStatus.PENDENTE)).thenReturn(charges);
        when(chargeRepository.findByVencimentoBetween(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)))
                .thenReturn(charges);
        when(financeSettingsService.getDiasParaAtrasoValue()).thenReturn(15);

        FinanceDashboardDTO result = financeDashboardService.generate("2026-08");

        assertThat(result.mensalidadesRecentes()).hasSize(5);
        assertThat(result.mensalidadesRecentes().get(0).vencimento()).isEqualTo(LocalDate.of(2026, 8, 30));
        assertThat(result.mensalidadesRecentes().get(4).vencimento()).isEqualTo(LocalDate.of(2026, 8, 10));
    }

    @Test
    void generate_bancoVazioModoTudo_retornaTudoZerado() {
        when(chargeRepository.findByStatusOrderByVencimentoAsc(Charge.ChargeStatus.PAGO)).thenReturn(List.of());
        when(expenseRepository.findAllByOrderByDataDesc()).thenReturn(List.of());

        FinanceDashboardDTO result = financeDashboardService.generate("TUDO");

        assertThat(result.entradas()).isEqualByComparingTo("0");
        assertThat(result.saidas()).isEqualByComparingTo("0");
        assertThat(result.saldoAcumulado()).isEqualByComparingTo("0");
        assertThat(result.variacaoPercentual()).isEqualByComparingTo("0");
        assertThat(result.aReceberContagem()).isZero();
        assertThat(result.mensalidadesRecentes()).isEmpty();
    }

    @Test
    void generate_periodoInvalido_throwsIllegalArgumentException() {
        when(chargeRepository.findByStatusOrderByVencimentoAsc(Charge.ChargeStatus.PAGO)).thenReturn(List.of());
        when(expenseRepository.findAllByOrderByDataDesc()).thenReturn(List.of());

        assertThatThrownBy(() -> financeDashboardService.generate("não-é-um-mês"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void generate_periodoNulo_usaMesAtualComoDefault() {
        when(chargeRepository.findByStatusOrderByVencimentoAsc(Charge.ChargeStatus.PAGO)).thenReturn(List.of());
        when(expenseRepository.findAllByOrderByDataDesc()).thenReturn(List.of());
        when(chargeRepository.findByStatusOrderByVencimentoAsc(Charge.ChargeStatus.PENDENTE)).thenReturn(List.of());
        when(chargeRepository.findByVencimentoBetween(YearMonth.now().atDay(1), YearMonth.now().atEndOfMonth()))
                .thenReturn(List.of());
        when(financeSettingsService.getDiasParaAtrasoValue()).thenReturn(15);

        FinanceDashboardDTO result = financeDashboardService.generate(null);

        assertThat(result.periodo()).isEqualTo(YearMonth.now().toString());
    }

    private Charge buildCharge(Long id, Charge.ChargeStatus status, LocalDate vencimento) {
        Charge charge = new Charge();
        charge.setId(id);
        charge.setPatient(patient);
        charge.setValor(new BigDecimal("100.00"));
        charge.setVencimento(vencimento);
        charge.setStatus(status);
        return charge;
    }

    private Expense buildExpense(BigDecimal valor, LocalDate data) {
        Expense expense = new Expense();
        expense.setValor(valor);
        expense.setData(data);
        return expense;
    }
}
