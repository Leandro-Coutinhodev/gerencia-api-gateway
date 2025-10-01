package com.app.gerencia.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.util.Date;

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

    @Column(name = "interview_date", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date interviewDate;

    @Lob
    @Column(name = "report")
    private byte[] report;

    @Column(columnDefinition = "TEXT")
    private String diagnoses; // e.g. autism, ADHD, etc

    @Column(columnDefinition = "TEXT")
    private String medicationAndAllergies;

    @Column(columnDefinition = "TEXT")
    private String indications;

    @Column(columnDefinition = "TEXT")
    private String objectives;

    @Column(columnDefinition = "TEXT")
    private String developmentHistory;

    @Column(columnDefinition = "TEXT")
    private String preferences;

    @Column(columnDefinition = "TEXT")
    private String interferingBehaviors;

    @Column(columnDefinition = "TEXT")
    private String qualityOfLife;

    @Column(columnDefinition = "TEXT")
    private String feeding;

    @Column(columnDefinition = "TEXT")
    private String sleep;

    @Column(columnDefinition = "TEXT")
    private String therapists;



    @Column(name = "status")
    private Character status = 'E';


    // Getters and Setters
    public Character getStatus() {
        return status;
    }

    public void setStatus(Character status) {
        this.status = status;
    }
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

    public Date getInterviewDate() {
        return interviewDate;
    }

    public void setInterviewDate(Date interviewDate) {
        this.interviewDate = interviewDate;
    }

    public byte[] getReport() {
        return report;
    }

    public void setReport(byte[] report) {
        this.report = report;
    }

    public String getDiagnoses() {
        return diagnoses;
    }

    public void setDiagnoses(String diagnoses) {
        this.diagnoses = diagnoses;
    }

    public String getMedicationAndAllergies() {
        return medicationAndAllergies;
    }

    public void setMedicationAndAllergies(String medicationAndAllergies) {
        this.medicationAndAllergies = medicationAndAllergies;
    }

    public String getIndications() {
        return indications;
    }

    public void setIndications(String indications) {
        this.indications = indications;
    }

    public String getObjectives() {
        return objectives;
    }

    public void setObjectives(String objectives) {
        this.objectives = objectives;
    }

    public String getDevelopmentHistory() {
        return developmentHistory;
    }

    public void setDevelopmentHistory(String developmentHistory) {
        this.developmentHistory = developmentHistory;
    }

    public String getPreferences() {
        return preferences;
    }

    public void setPreferences(String preferences) {
        this.preferences = preferences;
    }

    public String getInterferingBehaviors() {
        return interferingBehaviors;
    }

    public void setInterferingBehaviors(String interferingBehaviors) {
        this.interferingBehaviors = interferingBehaviors;
    }

    public String getQualityOfLife() {
        return qualityOfLife;
    }

    public void setQualityOfLife(String qualityOfLife) {
        this.qualityOfLife = qualityOfLife;
    }

    public String getFeeding() {
        return feeding;
    }

    public void setFeeding(String feeding) {
        this.feeding = feeding;
    }

    public String getSleep() {
        return sleep;
    }

    public void setSleep(String sleep) {
        this.sleep = sleep;
    }

    public String getTherapists() {
        return therapists;
    }

    public void setTherapists(String therapists) {
        this.therapists = therapists;
    }
}
