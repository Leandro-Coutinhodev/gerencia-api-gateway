package com.app.gerencia.repository;

import com.app.gerencia.entities.ContractParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContractParticipantRepository
        extends JpaRepository<ContractParticipant, Long> {

    Optional<ContractParticipant> findByToken(String token);

    List<ContractParticipant> findByContractIdOrderBySigningOrder(Long contractId);

    //  próximo da fila
    Optional<ContractParticipant>
    findFirstByContractIdAndSignedFalseOrderBySigningOrderAsc(Long contractId);

    // todos do contrato
    List<ContractParticipant> findByContractId(Long contractId);
}

