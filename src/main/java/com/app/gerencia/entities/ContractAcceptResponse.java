package com.app.gerencia.entities;

import jakarta.persistence.*;

/**
 * Resposta de um participante a um campo de aceite.
 */
@Entity
@Table(name = "tb_contract_accept_response")
public class ContractAcceptResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contract_accept_response_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_participant_id", nullable = false)
    private ContractParticipant participant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_accept_field_id", nullable = false)
    private ContractAcceptField acceptField;

    /**
     * Valor da resposta serializado como String.
     * true/false para CHECKBOX/SIM_NAO, texto livre para TEXT/DATE.
     */
    @Column(name = "response_value", columnDefinition = "TEXT")
    private String responseValue;

    // ── Getters/Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ContractParticipant getParticipant() { return participant; }
    public void setParticipant(ContractParticipant participant) { this.participant = participant; }

    public ContractAcceptField getAcceptField() { return acceptField; }
    public void setAcceptField(ContractAcceptField acceptField) { this.acceptField = acceptField; }

    public String getResponseValue() { return responseValue; }
    public void setResponseValue(String responseValue) { this.responseValue = responseValue; }
}
