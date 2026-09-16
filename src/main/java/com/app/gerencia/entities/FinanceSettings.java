package com.app.gerencia.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_finance_settings")
public class FinanceSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dias_para_atraso", nullable = false)
    private Integer diasParaAtraso;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getDiasParaAtraso() { return diasParaAtraso; }
    public void setDiasParaAtraso(Integer diasParaAtraso) { this.diasParaAtraso = diasParaAtraso; }
}
