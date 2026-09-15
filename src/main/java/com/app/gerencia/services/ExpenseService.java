package com.app.gerencia.services;

import com.app.gerencia.controllers.dto.CreateExpenseRequestDTO;
import com.app.gerencia.controllers.dto.ExpenseDTO;
import com.app.gerencia.entities.Expense;
import com.app.gerencia.repository.ExpenseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Transactional
    public ExpenseDTO create(CreateExpenseRequestDTO req) {
        validate(req);

        Expense expense = new Expense();
        expense.setValor(req.valor());
        expense.setCategoria(req.categoria());
        expense.setData(req.data());

        expense = expenseRepository.save(expense);
        return ExpenseDTO.fromEntity(expense);
    }

    public List<ExpenseDTO> findAll() {
        return expenseRepository.findAllByOrderByDataDesc().stream().map(ExpenseDTO::fromEntity).toList();
    }

    @Transactional
    public ExpenseDTO update(Long id, CreateExpenseRequestDTO req) {
        validate(req);

        Expense expense = findEntityById(id);
        expense.setValor(req.valor());
        expense.setCategoria(req.categoria());
        expense.setData(req.data());

        expense = expenseRepository.save(expense);
        return ExpenseDTO.fromEntity(expense);
    }

    @Transactional
    public void delete(Long id) {
        expenseRepository.delete(findEntityById(id));
    }

    private Expense findEntityById(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Despesa não encontrada: " + id));
    }

    private void validate(CreateExpenseRequestDTO req) {
        if (req.valor() == null || req.valor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor deve ser maior que zero");
        }
        if (req.categoria() == null || req.categoria().isBlank()) {
            throw new IllegalArgumentException("Categoria é obrigatória");
        }
    }
}
