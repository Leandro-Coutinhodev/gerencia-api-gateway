package com.app.gerencia.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Modelo reutilizável de contrato.
 * Contém cláusulas, variáveis dinâmicas, campos de aceite e
 * configuração de testemunhas/ordem de assinatura.
 */
@Entity
@Table(name = "tb_contract_template")
public class ContractTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contract_template_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Tipo do contrato: PRESTACAO_SERVICO, CONSENTIMENTO, LGPD, etc.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContractType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContractTemplateStatus status = ContractTemplateStatus.ATIVO;

    /**
     * Modo de assinatura dos participantes.
     * SEQUENCIAL = um de cada vez em ordem.
     * PARALELO   = todos recebem o link ao mesmo tempo.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "signing_mode", nullable = false)
    private SigningMode signingMode = SigningMode.SEQUENCIAL;

    /**
     * Configuração de testemunhas para este modelo.
     * OBRIGATORIO  = sempre exige testemunhas.
     * OPCIONAL     = pergunta ao criar o contrato.
     * NAO_UTILIZA  = sem testemunhas.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "witness_config", nullable = false)
    private WitnessConfig witnessConfig = WitnessConfig.NAO_UTILIZA;

    /**
     * Quantidade de testemunhas requeridas.
     * Relevante quando witnessConfig = OBRIGATORIO ou OPCIONAL.
     */
    @Column(name = "witness_count")
    private Integer witnessCount = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Relacionamentos ────────────────────────────────────────────────────

    /** Cláusulas do contrato, ordenadas por `order`. */
    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("clauseOrder ASC")
    private List<ContractClause> clauses;

    /** Variáveis dinâmicas ({{paciente_nome}}, {{valor_plano}}, etc.). */
    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContractVariable> variables;

    /** Campos de aceite (checkbox, sim/não, texto, data, assinatura). */
    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fieldOrder ASC")
    private List<ContractAcceptField> acceptFields;

    /** Contratos gerados a partir deste modelo. */
    @OneToMany(mappedBy = "template", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Contract> contracts;

    // ── Enums internos ─────────────────────────────────────────────────────

    public enum ContractTemplateStatus { ATIVO, INATIVO }

    public enum SigningMode { SEQUENCIAL, PARALELO }

    public enum WitnessConfig { OBRIGATORIO, OPCIONAL, NAO_UTILIZA }

    public enum ContractType {
        PRESTACAO_SERVICO, CONSENTIMENTO, LGPD, ANAMNESE, OUTRO
    }

    // ── Construtores ───────────────────────────────────────────────────────

    public ContractTemplate() {
        this.createdAt = LocalDateTime.now();
    }

    // ── Getters/Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ContractType getType() { return type; }
    public void setType(ContractType type) { this.type = type; }

    public ContractTemplateStatus getStatus() { return status; }
    public void setStatus(ContractTemplateStatus status) { this.status = status; }

    public SigningMode getSigningMode() { return signingMode; }
    public void setSigningMode(SigningMode signingMode) { this.signingMode = signingMode; }

    public WitnessConfig getWitnessConfig() { return witnessConfig; }
    public void setWitnessConfig(WitnessConfig witnessConfig) { this.witnessConfig = witnessConfig; }

    public Integer getWitnessCount() { return witnessCount; }
    public void setWitnessCount(Integer witnessCount) { this.witnessCount = witnessCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<ContractClause> getClauses() { return clauses; }
    public void setClauses(List<ContractClause> clauses) { this.clauses = clauses; }

    public List<ContractVariable> getVariables() { return variables; }
    public void setVariables(List<ContractVariable> variables) { this.variables = variables; }

    public List<ContractAcceptField> getAcceptFields() { return acceptFields; }
    public void setAcceptFields(List<ContractAcceptField> acceptFields) { this.acceptFields = acceptFields; }

    public List<Contract> getContracts() { return contracts; }
    public void setContracts(List<Contract> contracts) { this.contracts = contracts; }
}