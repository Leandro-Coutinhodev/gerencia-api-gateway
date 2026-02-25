package com.app.gerencia.entities;

import com.app.gerencia.enums.ContractStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tb_contract")
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @Lob
//    @Column(nullable = false)
//    private String clauses;
//
//    @Column(nullable = false, unique = true)
//    private String hash;

    @Column(name = "created_ip")
    private String createdIp;

    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private ContractStatus status;

    @ManyToOne
    @JoinColumn(name = "guardian_id")
    private Guardian guardian;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<ContractParticipant> participants;

    @Column(unique = true)
    private String hash;

    public String getPdfPath() {
        return pdfPath;
    }

    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    /**
     * Caminho do PDF final assinado no filesystem.
     */
    @Column(name = "pdf_path")
    private String pdfPath;

    // getters e setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

//    public String getClauses() {
//        return clauses;
//    }
//
//    public void setClauses(String clauses) {
//        this.clauses = clauses;
//    }
//
//    public String getHash() {
//        return hash;
//    }
//
//    public void setHash(String hash) {
//        this.hash = hash;
//    }

    public String getCreatedIp() {
        return createdIp;
    }

    public void setCreatedIp(String createdIp) {
        this.createdIp = createdIp;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ContractStatus getStatus() {
        return status;
    }

    public void setStatus(ContractStatus status) {
        this.status = status;
    }

    public Guardian getGuardian() {
        return guardian;
    }

    public void setGuardian(Guardian guardian) {
        this.guardian = guardian;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public List<ContractParticipant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<ContractParticipant> participants) {
        this.participants = participants;
    }
}
