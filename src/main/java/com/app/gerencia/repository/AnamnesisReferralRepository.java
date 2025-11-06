package com.app.gerencia.repository;

import com.app.gerencia.entities.AnamnesisReferral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface AnamnesisReferralRepository extends JpaRepository<AnamnesisReferral, Long> {

    Optional<AnamnesisReferral> findByAnamnesisId(Long anamnesisId);
}

