package com.app.gerencia.repository;

import com.app.gerencia.entities.FinanceSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FinanceSettingsRepository extends JpaRepository<FinanceSettings, Long> {
    Optional<FinanceSettings> findFirstByOrderByIdAsc();
}
