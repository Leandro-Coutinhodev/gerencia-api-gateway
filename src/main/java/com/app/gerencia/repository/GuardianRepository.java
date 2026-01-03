package com.app.gerencia.repository;

import com.app.gerencia.entities.Guardian;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GuardianRepository extends JpaRepository<Guardian, Long> {
    List<Guardian> findByNameContainingIgnoreCase(String name);
    List<Guardian> findByCpfContaining(String cpf);
    Optional<Guardian> findByCpf(String cpf);

    // Buscar por nome OU CPF
    List<Guardian> findByNameContainingIgnoreCaseOrCpfContaining(String name, String cpf);
}
