package com.app.gerencia.entities;

import com.app.gerencia.enums.ParticipantRole;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.util.List;


@Entity
@Table(name = "tb_contract_participant")
public class ContractParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contract_participant_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    @JsonBackReference
    private Contract contract;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantRole role;


    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    private String cpf;


    @Column(name = "signing_order", nullable = false)
    private Integer signingOrder;

    // Token UUID para link de assinatura
    @Column(unique = true, nullable = false)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "signing_status", nullable = false)
    private SigningStatus signingStatus = SigningStatus.PENDENTE;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guardian_id")
    private Guardian guardian;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    @OneToOne(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true)
    private ContractSignature signature;


    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContractAcceptResponse> acceptResponses;

    public enum SigningStatus { PENDENTE, ASSINADO, REJEITADO }



    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Contract getContract() { return contract; }
    public void setContract(Contract contract) { this.contract = contract; }

    public ParticipantRole getRole() { return role; }
    public void setRole(ParticipantRole role) { this.role = role; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public Integer getSigningOrder() { return signingOrder; }
    public void setSigningOrder(Integer signingOrder) { this.signingOrder = signingOrder; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public SigningStatus getSigningStatus() { return signingStatus; }
    public void setSigningStatus(SigningStatus signingStatus) { this.signingStatus = signingStatus; }

    public Guardian getGuardian() { return guardian; }
    public void setGuardian(Guardian guardian) { this.guardian = guardian; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public ContractSignature getSignature() { return signature; }
    public void setSignature(ContractSignature signature) { this.signature = signature; }

    public List<ContractAcceptResponse> getAcceptResponses() { return acceptResponses; }
    public void setAcceptResponses(List<ContractAcceptResponse> acceptResponses) { this.acceptResponses = acceptResponses; }

    public boolean isSigned() { return SigningStatus.ASSINADO == this.signingStatus; }
}
