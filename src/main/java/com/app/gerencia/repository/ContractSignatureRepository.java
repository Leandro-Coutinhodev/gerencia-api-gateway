package com.app.gerencia.repository;

import com.app.gerencia.entities.ContractSignature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContractSignatureRepository extends JpaRepository<ContractSignature, Long> {
    Optional<ContractSignature> findByParticipantId(Long participantId);
}

