package com.app.gerencia.repository;

import com.app.gerencia.entities.Guardian;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GuardianRepository extends JpaRepository<Guardian, Long> {
    List<Guardian> findByNameContainingIgnoreCase(String name);
}
