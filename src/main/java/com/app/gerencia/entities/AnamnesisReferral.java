package com.app.gerencia.entities;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "anamnesis_referral")
public class AnamnesisReferral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anamnesis_id", nullable = false)
    private Anamnesis anamnesis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assistant_id", nullable = false)
    private Assistant assistant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_id", nullable = false)
    private Professional professional;

    @Column(columnDefinition = "TEXT", name = "selected_fields")
    private String selectedFieldsJson;

    @Column(name = "sent_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date sentAt = new Date();
}
