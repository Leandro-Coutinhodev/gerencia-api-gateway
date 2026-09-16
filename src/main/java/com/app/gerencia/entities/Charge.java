package com.app.gerencia.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_charge")
public class Charge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "charge_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(nullable = false)
    private BigDecimal valor;

    private BigDecimal desconto;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento")
    private PaymentMethod formaPagamento;

    @Column(nullable = false)
    private LocalDate vencimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChargeStatus status = ChargeStatus.PENDENTE;

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public enum ChargeStatus { PENDENTE, PAGO }

    public enum PaymentMethod { PIX, CARTAO, BOLETO, DINHEIRO }

    public Charge() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public BigDecimal getDesconto() { return desconto; }
    public void setDesconto(BigDecimal desconto) { this.desconto = desconto; }

    public PaymentMethod getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(PaymentMethod formaPagamento) { this.formaPagamento = formaPagamento; }

    public LocalDate getVencimento() { return vencimento; }
    public void setVencimento(LocalDate vencimento) { this.vencimento = vencimento; }

    public ChargeStatus getStatus() { return status; }
    public void setStatus(ChargeStatus status) { this.status = status; }

    public LocalDate getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDate dataPagamento) { this.dataPagamento = dataPagamento; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
