package com.app.gerencia.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "tb_anamnesis")
public class Anamnesis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "anamnesis_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private AnamnesisTemplate template;


    @OneToMany(mappedBy = "anamnesis", fetch = FetchType.LAZY)
    private List<AnamnesisAnswer> answers = new ArrayList<>();

    @Column(name = "interview_date")
    private Date interviewDate;

    @Column(name = "status")
    private Character status = 'E';

    @OneToOne(mappedBy = "anamnesis", cascade = CascadeType.ALL, orphanRemoval = true)
    private AnamnesisReferral referral;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public AnamnesisTemplate getTemplate() {
        return template;
    }

    public void setTemplate(AnamnesisTemplate template) {
        this.template = template;
    }

    public List<AnamnesisAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnamnesisAnswer> answers) {
        this.answers = answers;
    }

    public Date getInterviewDate() {
        return interviewDate;
    }

    public void setInterviewDate(Date interviewDate) {
        this.interviewDate = interviewDate;
    }

    public Character getStatus() {
        return status;
    }

    public void setStatus(Character status) {
        this.status = status;
    }

    public AnamnesisReferral getReferral() {
        return referral;
    }

    public void setReferral(AnamnesisReferral referral) {
        this.referral = referral;
    }
}