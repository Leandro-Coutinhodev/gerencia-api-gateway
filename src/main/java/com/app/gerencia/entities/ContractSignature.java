package com.app.gerencia.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Registro de auditoria da assinatura de um participante.
 */
@Entity
@Table(name = "tb_contract_signature")
public class ContractSignature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contract_signature_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_participant_id", nullable = false)
    private ContractParticipant participant;

    @Column(name = "signed_at", nullable = false)
    private LocalDateTime signedAt;

    @Column(name = "signed_ip", nullable = false)
    private String signedIp;

    /** Hash SHA-256 do conteúdo renderizado no momento da assinatura. */
    @Column(name = "document_hash", nullable = false, columnDefinition = "TEXT")
    private String documentHash;

    @Column(name = "accepted_terms", nullable = false)
    private Boolean acceptedTerms = false;

    // ── Getters/Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ContractParticipant getParticipant() { return participant; }
    public void setParticipant(ContractParticipant participant) { this.participant = participant; }

    public LocalDateTime getSignedAt() { return signedAt; }
    public void setSignedAt(LocalDateTime signedAt) { this.signedAt = signedAt; }

    public String getSignedIp() { return signedIp; }
    public void setSignedIp(String signedIp) { this.signedIp = signedIp; }

    public String getDocumentHash() { return documentHash; }
    public void setDocumentHash(String documentHash) { this.documentHash = documentHash; }

    public Boolean getAcceptedTerms() { return acceptedTerms; }
    public void setAcceptedTerms(Boolean acceptedTerms) { this.acceptedTerms = acceptedTerms; }
}
