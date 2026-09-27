package com.app.gerencia.repository;

import com.app.gerencia.entities.Contract;
import com.app.gerencia.enums.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    List<Contract> findByPatientIdOrderByCreatedAtDesc(Long patientId);
    List<Contract> findAllByOrderByCreatedAtDesc();
    List<Contract> findByStatusOrderByCreatedAtDesc(ContractStatus status);
}

