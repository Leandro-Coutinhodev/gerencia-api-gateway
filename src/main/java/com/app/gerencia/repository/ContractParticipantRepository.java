package com.app.gerencia.repository;

import com.app.gerencia.entities.ContractParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractParticipantRepository extends JpaRepository<ContractParticipant, Long> {
    Optional<ContractParticipant> findByToken(String token);
    List<ContractParticipant> findByContractIdOrderBySigningOrderAsc(Long contractId);


    @Query("SELECT p FROM ContractParticipant p WHERE p.contract.id = :contractId " +
            "AND p.signingStatus = 'PENDENTE' ORDER BY p.signingOrder ASC")
    List<ContractParticipant> findPendingOrderedByContractId(Long contractId);
}


