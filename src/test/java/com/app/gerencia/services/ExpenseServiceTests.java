package com.app.gerencia.services;

import com.app.gerencia.controllers.dto.CreateExpenseRequestDTO;
import com.app.gerencia.controllers.dto.ExpenseDTO;
import com.app.gerencia.entities.Expense;
import com.app.gerencia.repository.ExpenseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTests {

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private ExpenseService expenseService;

    @Test
    void create_valid_savesAndReturnsDto() {
        CreateExpenseRequestDTO req = new CreateExpenseRequestDTO(
                new BigDecimal("80.00"), "Material de escritório", LocalDate.of(2026, 8, 10));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(inv -> {
            Expense e = inv.getArgument(0);
            e.setId(3L);
            return e;
        });

        ExpenseDTO result = expenseService.create(req);

        assertThat(result.id()).isEqualTo(3L);
        assertThat(result.categoria()).isEqualTo("Material de escritório");
        assertThat(result.valor()).isEqualByComparingTo("80.00");
    }

    @Test
    void create_valorZero_throwsIllegalArgumentException() {
        CreateExpenseRequestDTO req = new CreateExpenseRequestDTO(BigDecimal.ZERO, "Categoria", LocalDate.now());

        assertThatThrownBy(() -> expenseService.create(req)).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(expenseRepository);
    }

    @Test
    void create_categoriaBlank_throwsIllegalArgumentException() {
        CreateExpenseRequestDTO req = new CreateExpenseRequestDTO(new BigDecimal("10"), "   ", LocalDate.now());

        assertThatThrownBy(() -> expenseService.create(req)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_categoriaNull_throwsIllegalArgumentException() {
        CreateExpenseRequestDTO req = new CreateExpenseRequestDTO(new BigDecimal("10"), null, LocalDate.now());

        assertThatThrownBy(() -> expenseService.create(req)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void update_valid_savesAndReturnsDto() {
        Expense expense = new Expense();
        expense.setId(1L);
        expense.setValor(new BigDecimal("50.00"));
        expense.setCategoria("Aluguel");
        expense.setData(LocalDate.of(2026, 8, 1));
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateExpenseRequestDTO req = new CreateExpenseRequestDTO(
                new BigDecimal("120.00"), "Manutenção", LocalDate.of(2026, 9, 1));
        ExpenseDTO result = expenseService.update(1L, req);

        assertThat(result.valor()).isEqualByComparingTo("120.00");
        assertThat(result.categoria()).isEqualTo("Manutenção");
        assertThat(result.data()).isEqualTo(LocalDate.of(2026, 9, 1));
    }

    @Test
    void update_notFound_throwsEntityNotFoundException() {
        when(expenseRepository.findById(999L)).thenReturn(Optional.empty());
        CreateExpenseRequestDTO req = new CreateExpenseRequestDTO(new BigDecimal("10"), "Categoria", LocalDate.now());

        assertThatThrownBy(() -> expenseService.update(999L, req)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void update_valorZero_throwsIllegalArgumentException() {
        CreateExpenseRequestDTO req = new CreateExpenseRequestDTO(BigDecimal.ZERO, "Categoria", LocalDate.now());

        assertThatThrownBy(() -> expenseService.update(1L, req)).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(expenseRepository);
    }

    @Test
    void delete_valid_removesExpense() {
        Expense expense = new Expense();
        expense.setId(1L);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));

        expenseService.delete(1L);

        verify(expenseRepository).delete(expense);
    }

    @Test
    void delete_notFound_throwsEntityNotFoundException() {
        when(expenseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.delete(999L)).isInstanceOf(EntityNotFoundException.class);
        verify(expenseRepository, never()).delete(any());
    }

    @Test
    void findAll_returnsAllOrderedByDataDesc() {
        Expense expense = new Expense();
        expense.setId(1L);
        expense.setValor(new BigDecimal("50"));
        expense.setCategoria("Aluguel");
        expense.setData(LocalDate.now());
        when(expenseRepository.findAllByOrderByDataDesc()).thenReturn(List.of(expense));

        List<ExpenseDTO> result = expenseService.findAll();

        assertThat(result).hasSize(1);
        verify(expenseRepository).findAllByOrderByDataDesc();
    }
}
