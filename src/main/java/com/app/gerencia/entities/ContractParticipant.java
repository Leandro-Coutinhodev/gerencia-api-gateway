package com.app.gerencia.entities;

import com.app.gerencia.entities.Contract;
import com.app.gerencia.entities.Guardian;
import com.app.gerencia.entities.User;
import com.app.gerencia.enums.ParticipantRole;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_contract_participant")
public class ContractParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "contract_id")
    @JsonBackReference
    private Contract contract;

    @Enumerated(EnumType.STRING)
    private ParticipantRole role;

    // Guardian ou User (Secretary)
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @ManyToOne
    @JoinColumn(name = "guardian_id", nullable = true)
    private Guardian guardian;

    private Integer signingOrder;

    @Column(unique = true)
    private String token;

    private Boolean signed = false;

    private LocalDateTime signedAt;

    private String signedIp;

    // getters e setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public ParticipantRole getRole() {
        return role;
    }

    public void setRole(ParticipantRole role) {
        this.role = role;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Guardian getGuardian() {
        return guardian;
    }

    public void setGuardian(Guardian guardian) {
        this.guardian = guardian;
    }

    public Integer getSigningOrder() {
        return signingOrder;
    }

    public void setSigningOrder(Integer signingOrder) {
        this.signingOrder = signingOrder;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Boolean getSigned() {
        return signed;
    }

    public void setSigned(Boolean signed) {
        this.signed = signed;
    }

    public LocalDateTime getSignedAt() {
        return signedAt;
    }

    public void setSignedAt(LocalDateTime signedAt) {
        this.signedAt = signedAt;
    }

    public String getSignedIp() {
        return signedIp;
    }

    public void setSignedIp(String signedIp) {
        this.signedIp = signedIp;
    }
}
