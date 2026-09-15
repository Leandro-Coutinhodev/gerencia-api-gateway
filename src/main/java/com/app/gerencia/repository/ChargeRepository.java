package com.app.gerencia.repository;

import com.app.gerencia.entities.Charge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ChargeRepository extends JpaRepository<Charge, Long> {
    List<Charge> findAllByOrderByVencimentoAsc();
    List<Charge> findByStatusOrderByVencimentoAsc(Charge.ChargeStatus status);
    boolean existsByPatientId(Long patientId);
    List<Charge> findByStatusAndDataPagamentoBetween(Charge.ChargeStatus status, LocalDate start, LocalDate end);
    List<Charge> findByVencimentoBetween(LocalDate start, LocalDate end);
}
