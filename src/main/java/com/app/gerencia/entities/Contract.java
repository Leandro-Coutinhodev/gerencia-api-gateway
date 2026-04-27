package com.app.gerencia.entities;

import com.app.gerencia.enums.ContractStatus;
import com.app.gerencia.enums.ContractType;
import com.fasterxml.jackson.annotation.JsonBackReference;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guardian_id")
    private Guardian guardian;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<ContractParticipant> participants;

    @Column(unique = true)
    private String hash;

    @Enumerated(EnumType.STRING)
    private ContractType type;

    private Boolean hasWitnesses;

    @Lob
    @Column(name = "pdf_data")
    private byte[] pdfData;

    private String pdfFileName;

    private String pdfContentType;

    public byte[] getPdfData() {
        return pdfData;
    }

    public void setPdfData(byte[] pdfData) {
        this.pdfData = pdfData;
    }

    public String getPdfFileName() {
        return pdfFileName;
    }

    public void setPdfFileName(String pdfFileName) {
        this.pdfFileName = pdfFileName;
    }

    public String getPdfContentType() {
        return pdfContentType;
    }

    public void setPdfContentType(String pdfContentType) {
        this.pdfContentType = pdfContentType;
    }

    public ContractType getType() {
        return type;
    }

    public void setType(ContractType type) {
        this.type = type;
    }

    public Boolean getHasWitnesses() {
        return hasWitnesses;
    }

    public void setHasWitnesses(Boolean hasWitnesses) {
        this.hasWitnesses = hasWitnesses;
    }

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
