package com.app.gerencia.services;

import com.app.gerencia.controllers.dto.ChargeDTO;
import com.app.gerencia.controllers.dto.CreateChargeRequestDTO;
import com.app.gerencia.controllers.dto.RegisterPaymentRequestDTO;
import com.app.gerencia.entities.Charge;
import com.app.gerencia.entities.Patient;
import com.app.gerencia.repository.ChargeRepository;
import com.app.gerencia.repository.PatientRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ChargeService {

    private final ChargeRepository chargeRepository;
    private final PatientRepository patientRepository;
    private final ChargeReceiptPdfService chargeReceiptPdfService;
    private final FinanceSettingsService financeSettingsService;

    public ChargeService(ChargeRepository chargeRepository, PatientRepository patientRepository,
                          ChargeReceiptPdfService chargeReceiptPdfService, FinanceSettingsService financeSettingsService) {
        this.chargeRepository = chargeRepository;
        this.patientRepository = patientRepository;
        this.chargeReceiptPdfService = chargeReceiptPdfService;
        this.financeSettingsService = financeSettingsService;
    }

    @Transactional
    public ChargeDTO create(CreateChargeRequestDTO req) {
        validate(req);

        Patient patient = patientRepository.findById(req.patientId())
                .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado"));

        Charge charge = new Charge();
        charge.setPatient(patient);
        charge.setValor(req.valor());
        charge.setDesconto(req.desconto());
        charge.setFormaPagamento(req.formaPagamento() != null ? Charge.PaymentMethod.valueOf(req.formaPagamento()) : null);
        charge.setVencimento(req.vencimento());
        charge.setStatus(Charge.ChargeStatus.PENDENTE);

        charge = chargeRepository.save(charge);
        return ChargeDTO.fromEntity(charge, financeSettingsService.getDiasParaAtrasoValue());
    }

    public List<ChargeDTO> findAll(String status) {
        List<Charge> charges = (status == null || status.isBlank())
                ? chargeRepository.findAllByOrderByVencimentoAsc()
                : chargeRepository.findByStatusOrderByVencimentoAsc(Charge.ChargeStatus.valueOf(status));
        int diasParaAtraso = financeSettingsService.getDiasParaAtrasoValue();
        return charges.stream().map(c -> ChargeDTO.fromEntity(c, diasParaAtraso)).toList();
    }

    @Transactional
    public ChargeDTO registerPayment(Long id, RegisterPaymentRequestDTO req) {
        Charge charge = findEntityById(id);
        charge.setStatus(Charge.ChargeStatus.PAGO);
        charge.setFormaPagamento(Charge.PaymentMethod.valueOf(req.formaPagamento()));
        charge.setDataPagamento(req.dataPagamento());
        charge = chargeRepository.save(charge);
        return ChargeDTO.fromEntity(charge, financeSettingsService.getDiasParaAtrasoValue());
    }

    @Transactional
    public ChargeDTO update(Long id, CreateChargeRequestDTO req) {
        validate(req);

        Charge charge = findEntityById(id);
        if (charge.getStatus() != Charge.ChargeStatus.PENDENTE) {
            throw new IllegalStateException("Cobrança já paga não pode ser editada");
        }

        Patient patient = patientRepository.findById(req.patientId())
                .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado"));

        charge.setPatient(patient);
        charge.setValor(req.valor());
        charge.setDesconto(req.desconto());
        charge.setFormaPagamento(req.formaPagamento() != null ? Charge.PaymentMethod.valueOf(req.formaPagamento()) : null);
        charge.setVencimento(req.vencimento());

        charge = chargeRepository.save(charge);
        return ChargeDTO.fromEntity(charge, financeSettingsService.getDiasParaAtrasoValue());
    }

    @Transactional
    public void delete(Long id) {
        Charge charge = findEntityById(id);
        if (charge.getStatus() != Charge.ChargeStatus.PENDENTE) {
            throw new IllegalStateException("Cobrança já paga não pode ser excluída");
        }
        chargeRepository.delete(charge);
    }

    public byte[] generateReceipt(Long id) {
        Charge charge = findEntityById(id);
        if (charge.getStatus() != Charge.ChargeStatus.PAGO) {
            throw new IllegalStateException("Cobrança ainda não foi paga");
        }
        return chargeReceiptPdfService.generate(charge);
    }

    private void validate(CreateChargeRequestDTO req) {
        if (req.valor() == null || req.valor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor deve ser maior que zero");
        }
        if (req.patientId() == null) {
            throw new IllegalArgumentException("Paciente é obrigatório");
        }
    }

    private Charge findEntityById(Long id) {
        return chargeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cobrança não encontrada: " + id));
    }
}
