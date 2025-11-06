package com.app.gerencia.entities;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "tb_anamnesis_referral")
public class AnamnesisReferral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "anamnesis_referral_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anamnesis_id", nullable = false)
    private Anamnesis anamnesis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assistant_id", nullable = true)
    private Assistant assistant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_id", nullable = false)
    private Professional professional;

    @Column(columnDefinition = "TEXT", name = "selected_fields")
    private String selectedFieldsJson;

    @Column(name = "sent_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date sentAt = new Date();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Anamnesis getAnamnesis() {
        return anamnesis;
    }

    public void setAnamnesis(Anamnesis anamnesis) {
        this.anamnesis = anamnesis;
    }

    public Assistant getAssistant() {
        return assistant;
    }

    public void setAssistant(Assistant assistant) {
        this.assistant = assistant;
    }

    public Professional getProfessional() {
        return professional;
    }

    public void setProfessional(Professional professional) {
        this.professional = professional;
    }

    public String getSelectedFieldsJson() {
        return selectedFieldsJson;
    }

    public void setSelectedFieldsJson(String selectedFieldsJson) {
        this.selectedFieldsJson = selectedFieldsJson;
    }

    public Date getSentAt() {
        return sentAt;
    }

    public void setSentAt(Date sentAt) {
        this.sentAt = sentAt;
    }
}
