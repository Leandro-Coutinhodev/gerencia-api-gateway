package com.app.gerencia.services;

import com.app.gerencia.controllers.dto.ChargeDTO;
import com.app.gerencia.controllers.dto.CreateChargeRequestDTO;
import com.app.gerencia.controllers.dto.RegisterPaymentRequestDTO;
import com.app.gerencia.entities.Charge;
import com.app.gerencia.entities.Guardian;
import com.app.gerencia.entities.Patient;
import com.app.gerencia.repository.ChargeRepository;
import com.app.gerencia.repository.PatientRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChargeServiceTests {

    @Mock
    private ChargeRepository chargeRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private ChargeReceiptPdfService chargeReceiptPdfService;

    @Mock
    private FinanceSettingsService financeSettingsService;

    @InjectMocks
    private ChargeService chargeService;

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
    void create_valid_savesAndReturnsDto() {
        CreateChargeRequestDTO req = new CreateChargeRequestDTO(
                1L, new BigDecimal("250.00"), null, "PIX", LocalDate.of(2026, 9, 20));
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(chargeRepository.save(any(Charge.class))).thenAnswer(inv -> {
            Charge c = inv.getArgument(0);
            c.setId(10L);
            return c;
        });
        when(financeSettingsService.getDiasParaAtrasoValue()).thenReturn(15);

        ChargeDTO result = chargeService.create(req);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.patientName()).isEqualTo("João da Silva");
        assertThat(result.guardianName()).isEqualTo("Maria da Silva");
        assertThat(result.status()).isEqualTo(Charge.ChargeStatus.PENDENTE);
        assertThat(result.formaPagamento()).isEqualTo(Charge.PaymentMethod.PIX);
    }

    @Test
    void create_valorZero_throwsIllegalArgumentException() {
        CreateChargeRequestDTO req = new CreateChargeRequestDTO(1L, BigDecimal.ZERO, null, "PIX", LocalDate.now());

        assertThatThrownBy(() -> chargeService.create(req))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(chargeRepository);
    }

    @Test
    void create_valorNegative_throwsIllegalArgumentException() {
        CreateChargeRequestDTO req = new CreateChargeRequestDTO(1L, new BigDecimal("-10"), null, "PIX", LocalDate.now());

        assertThatThrownBy(() -> chargeService.create(req))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_patientNotFound_throwsIllegalArgumentException() {
        CreateChargeRequestDTO req = new CreateChargeRequestDTO(99L, new BigDecimal("100"), null, "PIX", LocalDate.now());
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chargeService.create(req))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(chargeRepository);
    }

    @Test
    void findAll_withoutStatus_delegatesToFindAllOrderedByVencimento() {
        Charge charge = buildCharge(1L, Charge.ChargeStatus.PENDENTE);
        when(chargeRepository.findAllByOrderByVencimentoAsc()).thenReturn(List.of(charge));
        when(financeSettingsService.getDiasParaAtrasoValue()).thenReturn(15);

        List<ChargeDTO> result = chargeService.findAll(null);

        assertThat(result).hasSize(1);
        verify(chargeRepository, never()).findByStatusOrderByVencimentoAsc(any());
    }

    @Test
    void findAll_withStatus_filtersByStatus() {
        Charge charge = buildCharge(1L, Charge.ChargeStatus.PAGO);
        when(chargeRepository.findByStatusOrderByVencimentoAsc(Charge.ChargeStatus.PAGO)).thenReturn(List.of(charge));
        when(financeSettingsService.getDiasParaAtrasoValue()).thenReturn(15);

        List<ChargeDTO> result = chargeService.findAll("PAGO");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).status()).isEqualTo(Charge.ChargeStatus.PAGO);
    }

    @Test
    void findAll_chargePendenteVencidaDentroDoPrazoParaAtraso_atrasadoFalse() {
        Charge charge = buildCharge(1L, Charge.ChargeStatus.PENDENTE, LocalDate.now().minusDays(5));
        when(chargeRepository.findAllByOrderByVencimentoAsc()).thenReturn(List.of(charge));
        when(financeSettingsService.getDiasParaAtrasoValue()).thenReturn(15);

        List<ChargeDTO> result = chargeService.findAll(null);

        assertThat(result.get(0).atrasado()).isFalse();
        assertThat(result.get(0).diasAtraso()).isEqualTo(0);
    }

    @Test
    void findAll_chargePendenteVencidaExatamenteNoLimiar_atrasadoFalse() {
        Charge charge = buildCharge(1L, Charge.ChargeStatus.PENDENTE, LocalDate.now().minusDays(15));
        when(chargeRepository.findAllByOrderByVencimentoAsc()).thenReturn(List.of(charge));
        when(financeSettingsService.getDiasParaAtrasoValue()).thenReturn(15);

        List<ChargeDTO> result = chargeService.findAll(null);

        assertThat(result.get(0).atrasado()).isFalse();
        assertThat(result.get(0).diasAtraso()).isEqualTo(0);
    }

    @Test
    void findAll_chargePendenteVencidaAlemDoPrazoParaAtraso_atrasadoTrue() {
        Charge charge = buildCharge(1L, Charge.ChargeStatus.PENDENTE, LocalDate.now().minusDays(20));
        when(chargeRepository.findAllByOrderByVencimentoAsc()).thenReturn(List.of(charge));
        when(financeSettingsService.getDiasParaAtrasoValue()).thenReturn(15);

        List<ChargeDTO> result = chargeService.findAll(null);

        assertThat(result.get(0).atrasado()).isTrue();
        assertThat(result.get(0).diasAtraso()).isEqualTo(20);
    }

    @Test
    void findAll_chargePagoComVencimentoNoPassado_atrasadoFalse() {
        Charge charge = buildCharge(1L, Charge.ChargeStatus.PAGO, LocalDate.now().minusDays(30));
        when(chargeRepository.findAllByOrderByVencimentoAsc()).thenReturn(List.of(charge));
        when(financeSettingsService.getDiasParaAtrasoValue()).thenReturn(15);

        List<ChargeDTO> result = chargeService.findAll(null);

        assertThat(result.get(0).atrasado()).isFalse();
        assertThat(result.get(0).diasAtraso()).isEqualTo(0);
    }

    @Test
    void registerPayment_marksAsPaidWithFormaPagamentoAndData() {
        Charge charge = buildCharge(5L, Charge.ChargeStatus.PENDENTE);
        when(chargeRepository.findById(5L)).thenReturn(Optional.of(charge));
        when(chargeRepository.save(any(Charge.class))).thenAnswer(inv -> inv.getArgument(0));
        when(financeSettingsService.getDiasParaAtrasoValue()).thenReturn(15);

        RegisterPaymentRequestDTO req = new RegisterPaymentRequestDTO("CARTAO", LocalDate.of(2026, 8, 25));
        ChargeDTO result = chargeService.registerPayment(5L, req);

        assertThat(result.status()).isEqualTo(Charge.ChargeStatus.PAGO);
        assertThat(result.formaPagamento()).isEqualTo(Charge.PaymentMethod.CARTAO);
        assertThat(result.dataPagamento()).isEqualTo(LocalDate.of(2026, 8, 25));
    }

    @Test
    void registerPayment_chargeNotFound_throwsEntityNotFoundException() {
        when(chargeRepository.findById(999L)).thenReturn(Optional.empty());
        RegisterPaymentRequestDTO req = new RegisterPaymentRequestDTO("PIX", LocalDate.now());

        assertThatThrownBy(() -> chargeService.registerPayment(999L, req))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void update_pendente_savesAndReturnsDto() {
        Charge charge = buildCharge(1L, Charge.ChargeStatus.PENDENTE);
        when(chargeRepository.findById(1L)).thenReturn(Optional.of(charge));
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(chargeRepository.save(any(Charge.class))).thenAnswer(inv -> inv.getArgument(0));
        when(financeSettingsService.getDiasParaAtrasoValue()).thenReturn(15);

        CreateChargeRequestDTO req = new CreateChargeRequestDTO(
                1L, new BigDecimal("300.00"), new BigDecimal("20.00"), "BOLETO", LocalDate.of(2026, 10, 1));
        ChargeDTO result = chargeService.update(1L, req);

        assertThat(result.valor()).isEqualByComparingTo("300.00");
        assertThat(result.desconto()).isEqualByComparingTo("20.00");
        assertThat(result.formaPagamento()).isEqualTo(Charge.PaymentMethod.BOLETO);
        assertThat(result.vencimento()).isEqualTo(LocalDate.of(2026, 10, 1));
    }

    @Test
    void update_pago_throwsIllegalStateException() {
        Charge charge = buildCharge(1L, Charge.ChargeStatus.PAGO);
        when(chargeRepository.findById(1L)).thenReturn(Optional.of(charge));

        CreateChargeRequestDTO req = new CreateChargeRequestDTO(1L, new BigDecimal("300.00"), null, "PIX", LocalDate.now());

        assertThatThrownBy(() -> chargeService.update(1L, req))
                .isInstanceOf(IllegalStateException.class);
        verify(chargeRepository, never()).save(any());
    }

    @Test
    void update_chargeNotFound_throwsEntityNotFoundException() {
        when(chargeRepository.findById(999L)).thenReturn(Optional.empty());
        CreateChargeRequestDTO req = new CreateChargeRequestDTO(1L, new BigDecimal("300.00"), null, "PIX", LocalDate.now());

        assertThatThrownBy(() -> chargeService.update(999L, req))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void delete_pendente_removesCharge() {
        Charge charge = buildCharge(1L, Charge.ChargeStatus.PENDENTE);
        when(chargeRepository.findById(1L)).thenReturn(Optional.of(charge));

        chargeService.delete(1L);

        verify(chargeRepository).delete(charge);
    }

    @Test
    void delete_pago_throwsIllegalStateException() {
        Charge charge = buildCharge(1L, Charge.ChargeStatus.PAGO);
        when(chargeRepository.findById(1L)).thenReturn(Optional.of(charge));

        assertThatThrownBy(() -> chargeService.delete(1L))
                .isInstanceOf(IllegalStateException.class);
        verify(chargeRepository, never()).delete(any());
    }

    @Test
    void delete_chargeNotFound_throwsEntityNotFoundException() {
        when(chargeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chargeService.delete(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void generateReceipt_whenPaid_delegatesToPdfService() {
        Charge charge = buildCharge(7L, Charge.ChargeStatus.PAGO);
        when(chargeRepository.findById(7L)).thenReturn(Optional.of(charge));
        byte[] fakePdf = "pdf".getBytes();
        when(chargeReceiptPdfService.generate(charge)).thenReturn(fakePdf);

        byte[] result = chargeService.generateReceipt(7L);

        assertThat(result).isEqualTo(fakePdf);
    }

    @Test
    void generateReceipt_whenNotPaid_throwsIllegalStateException() {
        Charge charge = buildCharge(8L, Charge.ChargeStatus.PENDENTE);
        when(chargeRepository.findById(8L)).thenReturn(Optional.of(charge));

        assertThatThrownBy(() -> chargeService.generateReceipt(8L))
                .isInstanceOf(IllegalStateException.class);
        verifyNoInteractions(chargeReceiptPdfService);
    }

    private Charge buildCharge(Long id, Charge.ChargeStatus status) {
        return buildCharge(id, status, LocalDate.now());
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
}
