package com.app.gerencia.repository;

import com.app.gerencia.entities.AnamnesisReferral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnamnesisReferralRepository extends JpaRepository<AnamnesisReferral, Long> {
}
