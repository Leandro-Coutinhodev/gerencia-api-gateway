package com.app.gerencia.services;

import com.app.gerencia.controllers.dto.ChargeDTO;
import com.app.gerencia.controllers.dto.FinanceDashboardDTO;
import com.app.gerencia.entities.Charge;
import com.app.gerencia.entities.Expense;
import com.app.gerencia.repository.ChargeRepository;
import com.app.gerencia.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class FinanceDashboardService {

    private static final int MENSALIDADES_RECENTES_LIMITE = 5;
    private static final DateTimeFormatter FORMATO_PERIODO = DateTimeFormatter.ofPattern("yyyy-MM");

    private final ChargeRepository chargeRepository;
    private final ExpenseRepository expenseRepository;
    private final FinanceSettingsService financeSettingsService;

    public FinanceDashboardService(ChargeRepository chargeRepository, ExpenseRepository expenseRepository,
                                    FinanceSettingsService financeSettingsService) {
        this.chargeRepository = chargeRepository;
        this.expenseRepository = expenseRepository;
        this.financeSettingsService = financeSettingsService;
    }

    public FinanceDashboardDTO generate(String periodoParam) {
        String periodo = (periodoParam == null || periodoParam.isBlank())
                ? YearMonth.now().toString()
                : periodoParam;
        boolean tudo = "TUDO".equalsIgnoreCase(periodo);

        List<Charge> pagos = chargeRepository.findByStatusOrderByVencimentoAsc(Charge.ChargeStatus.PAGO);
        List<Expense> despesas = expenseRepository.findAllByOrderByDataDesc();

        YearMonth mesReferencia = tudo ? mesMaisRecenteComMovimento(pagos, despesas) : parseMes(periodo);

        if (mesReferencia == null) {
            return new FinanceDashboardDTO(periodo, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, 0, 0, 0, 0, List.of());
        }

        LocalDate inicioRef = mesReferencia.atDay(1);
        LocalDate fimRef = mesReferencia.atEndOfMonth();

        BigDecimal saldoAntes = somaEntradas(pagos, null, inicioRef.minusDays(1))
                .subtract(somaSaidas(despesas, null, inicioRef.minusDays(1)));
        BigDecimal entradasRef = somaEntradas(pagos, inicioRef, fimRef);
        BigDecimal saidasRef = somaSaidas(despesas, inicioRef, fimRef);

        BigDecimal variacaoPercentual = saldoAntes.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : entradasRef.subtract(saidasRef)
                        .divide(saldoAntes, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));

        BigDecimal entradas;
        BigDecimal saidas;
        BigDecimal saldoAcumulado;
        if (tudo) {
            entradas = somaEntradas(pagos, null, null);
            saidas = somaSaidas(despesas, null, null);
            saldoAcumulado = entradas.subtract(saidas);
        } else {
            entradas = entradasRef;
            saidas = saidasRef;
            saldoAcumulado = saldoAntes.add(entradasRef).subtract(saidasRef);
        }

        List<Charge> pendentes = chargeRepository.findByStatusOrderByVencimentoAsc(Charge.ChargeStatus.PENDENTE);
        List<Charge> pendentesNoPeriodo = tudo
                ? pendentes
                : pendentes.stream().filter(c -> !c.getVencimento().isAfter(fimRef)).toList();
        BigDecimal aReceberValor = pendentesNoPeriodo.stream()
                .map(this::valorComDesconto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Charge> chargesDoPeriodo = tudo
                ? chargeRepository.findAllByOrderByVencimentoAsc()
                : chargeRepository.findByVencimentoBetween(inicioRef, fimRef);

        int diasParaAtraso = financeSettingsService.getDiasParaAtrasoValue();
        List<ChargeDTO> classificados = chargesDoPeriodo.stream()
                .map(c -> ChargeDTO.fromEntity(c, diasParaAtraso))
                .toList();

        long statusPagoContagem = classificados.stream().filter(dto -> dto.status() == Charge.ChargeStatus.PAGO).count();
        long statusAtrasadoContagem = classificados.stream().filter(ChargeDTO::atrasado).count();
        long statusPendenteContagem = classificados.size() - statusPagoContagem - statusAtrasadoContagem;

        List<ChargeDTO> mensalidadesRecentes = classificados.stream()
                .sorted(Comparator.comparing(ChargeDTO::vencimento).reversed())
                .limit(MENSALIDADES_RECENTES_LIMITE)
                .toList();

        return new FinanceDashboardDTO(
                periodo, entradas, saidas, saldoAcumulado, variacaoPercentual,
                aReceberValor, pendentesNoPeriodo.size(),
                statusPagoContagem, statusPendenteContagem, statusAtrasadoContagem,
                mensalidadesRecentes
        );
    }

    private YearMonth parseMes(String periodo) {
        try {
            return YearMonth.parse(periodo, FORMATO_PERIODO);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Período inválido: " + periodo);
        }
    }

    private YearMonth mesMaisRecenteComMovimento(List<Charge> pagos, List<Expense> despesas) {
        Optional<YearMonth> maxEntrada = pagos.stream()
                .map(c -> YearMonth.from(c.getDataPagamento()))
                .max(Comparator.naturalOrder());
        Optional<YearMonth> maxSaida = despesas.stream()
                .map(e -> YearMonth.from(e.getData()))
                .max(Comparator.naturalOrder());
        return Stream.of(maxEntrada, maxSaida)
                .flatMap(Optional::stream)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    private BigDecimal somaEntradas(List<Charge> pagos, LocalDate startInclusive, LocalDate endInclusive) {
        return pagos.stream()
                .filter(c -> dentroDoIntervalo(c.getDataPagamento(), startInclusive, endInclusive))
                .map(this::valorComDesconto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal somaSaidas(List<Expense> despesas, LocalDate startInclusive, LocalDate endInclusive) {
        return despesas.stream()
                .filter(e -> dentroDoIntervalo(e.getData(), startInclusive, endInclusive))
                .map(Expense::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean dentroDoIntervalo(LocalDate data, LocalDate startInclusive, LocalDate endInclusive) {
        if (data == null) {
            return false;
        }
        if (startInclusive != null && data.isBefore(startInclusive)) {
            return false;
        }
        return endInclusive == null || !data.isAfter(endInclusive);
    }

    private BigDecimal valorComDesconto(Charge c) {
        return c.getValor().subtract(c.getDesconto() != null ? c.getDesconto() : BigDecimal.ZERO);
    }
}
