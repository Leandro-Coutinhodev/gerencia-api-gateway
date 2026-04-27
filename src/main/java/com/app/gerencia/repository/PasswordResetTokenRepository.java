package com.app.gerencia.repository;

import com.app.gerencia.entities.PasswordRecovery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordRecovery, Long> {
}
