package com.app.gerencia.repository;

import com.app.gerencia.entities.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findAllByOrderByDataDesc();
    List<Expense> findByDataBetween(LocalDate start, LocalDate end);
}
