package com.app.gerencia.repository;

import com.app.gerencia.entities.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContractTemplateRepository extends JpaRepository<ContractTemplate, Long> {
    List<ContractTemplate> findByStatusOrderByCreatedAtDesc(ContractTemplate.ContractTemplateStatus status);
    Optional<ContractTemplate> findByName(String name);
}
