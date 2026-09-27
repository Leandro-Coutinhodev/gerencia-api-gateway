package com.app.gerencia.entities;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Ficha de atendimento preenchida: vincula um modelo, um paciente, o profissional que preencheu
// e a data da sessão. As respostas (campos gerais + campos de cada item de atividade) são
// guardadas como JSON em answersData, no mesmo espírito de Contract.variablesData.
@Entity
@Table(name = "tb_record")
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private RecordTemplate template;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_id", nullable = false)
    private Professional professional;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "answers_data", columnDefinition = "TEXT")
    private String answersData;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public RecordTemplate getTemplate() { return template; }
    public void setTemplate(RecordTemplate template) { this.template = template; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Professional getProfessional() { return professional; }
    public void setProfessional(Professional professional) { this.professional = professional; }

    public LocalDate getSessionDate() { return sessionDate; }
    public void setSessionDate(LocalDate sessionDate) { this.sessionDate = sessionDate; }

    public String getAnswersData() { return answersData; }
    public void setAnswersData(String answersData) { this.answersData = answersData; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
