package com.app.gerencia.entities;

import com.app.gerencia.enums.ContractStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Contrato gerado a partir de um modelo ou anexado externamente.
 */
@Entity
@Table(name = "tb_contract")
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contract_id")
    private Long id;

    /** Modelo usado para gerar este contrato. Null = PDF anexado externamente. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_template_id")
    private ContractTemplate template;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @JsonBackReference
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guardian_id", nullable = false)
    private Guardian guardian;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContractStatus status = ContractStatus.RASCUNHO;

    /**
     * JSON com os valores preenchidos para as variáveis dinâmicas.
     * Ex: {"valor_plano":"R$ 500,00","data_inicio":"01/01/2025"}
     */
    @Column(name = "variables_data", columnDefinition = "TEXT")
    private String variablesData;

    /**
     * Conteúdo final do contrato com variáveis já substituídas.
     * Gerado no momento do envio para assinatura.
     */
    @Column(name = "rendered_content", columnDefinition = "TEXT")
    private String renderedContent;

    /** PDF gerado com assinaturas eletrônicas (após conclusão). */
    @Lob
    @Column(name = "pdf_data")
    private byte[] pdfData;

    @Column(name = "pdf_file_name")
    private String pdfFileName;

    /**
     * PDF anexado externamente (fluxo "já assinado").
     */
    @Lob
    @Column(name = "external_pdf_data")
    private byte[] externalPdfData;

    @Column(name = "external_pdf_file_name")
    private String externalPdfFileName;

    /** Hash SHA-256 do conteúdo renderizado para integridade. */
    @Column(unique = true)
    private String hash;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_ip")
    private String createdIp;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    /** Se este contrato usa testemunhas (relevante quando modelo é OPCIONAL). */
    @Column(name = "has_witnesses")
    private Boolean hasWitnesses = false;

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @OrderBy("signingOrder ASC")
    private List<ContractParticipant> participants;

    // ── Construtores ───────────────────────────────────────────────────────

    public Contract() {
        this.createdAt = LocalDateTime.now();
    }

    // ── Status helpers ─────────────────────────────────────────────────────

    public boolean isCompleted() {
        return ContractStatus.ASSINADO == this.status
                || ContractStatus.ASSINADO_EXTERNAMENTE == this.status;
    }

    // ── Getters/Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ContractTemplate getTemplate() { return template; }
    public void setTemplate(ContractTemplate template) { this.template = template; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Guardian getGuardian() { return guardian; }
    public void setGuardian(Guardian guardian) { this.guardian = guardian; }

    public ContractStatus getStatus() { return status; }
    public void setStatus(ContractStatus status) { this.status = status; }

    public String getVariablesData() { return variablesData; }
    public void setVariablesData(String variablesData) { this.variablesData = variablesData; }

    public String getRenderedContent() { return renderedContent; }
    public void setRenderedContent(String renderedContent) { this.renderedContent = renderedContent; }

    public byte[] getPdfData() { return pdfData; }
    public void setPdfData(byte[] pdfData) { this.pdfData = pdfData; }

    public String getPdfFileName() { return pdfFileName; }
    public void setPdfFileName(String pdfFileName) { this.pdfFileName = pdfFileName; }

    public byte[] getExternalPdfData() { return externalPdfData; }
    public void setExternalPdfData(byte[] externalPdfData) { this.externalPdfData = externalPdfData; }

    public String getExternalPdfFileName() { return externalPdfFileName; }
    public void setExternalPdfFileName(String externalPdfFileName) { this.externalPdfFileName = externalPdfFileName; }

    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedIp() { return createdIp; }
    public void setCreatedIp(String createdIp) { this.createdIp = createdIp; }

    public Long getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(Long createdByUserId) { this.createdByUserId = createdByUserId; }

    public Boolean getHasWitnesses() { return hasWitnesses; }
    public void setHasWitnesses(Boolean hasWitnesses) { this.hasWitnesses = hasWitnesses; }

    public List<ContractParticipant> getParticipants() { return participants; }
    public void setParticipants(List<ContractParticipant> participants) { this.participants = participants; }
}
