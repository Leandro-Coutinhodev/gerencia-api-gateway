package com.app.gerencia.controllers;

import com.app.gerencia.controllers.dto.CreateExpenseRequestDTO;
import com.app.gerencia.controllers.dto.ExpenseDTO;
import com.app.gerencia.services.ExpenseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExpenseController.class)
@AutoConfigureMockMvc(addFilters = false)
class ExpenseControllerTests {

    private static final String BASE_URL = "/api-gateway/gerencia/financeiro/despesa";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ExpenseService expenseService;

    @Test
    void getAll_returnsList() throws Exception {
        ExpenseDTO dto = new ExpenseDTO(1L, new BigDecimal("80.00"), "Material", LocalDate.now(), LocalDateTime.now());
        when(expenseService.findAll()).thenReturn(List.of(dto));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoria").value("Material"));
    }

    @Test
    void create_valid_returns201() throws Exception {
        CreateExpenseRequestDTO req = new CreateExpenseRequestDTO(new BigDecimal("80.00"), "Material", LocalDate.now());
        ExpenseDTO dto = new ExpenseDTO(1L, req.valor(), req.categoria(), req.data(), LocalDateTime.now());
        when(expenseService.create(any())).thenReturn(dto);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoria").value("Material"));
    }

    @Test
    void create_serviceThrowsIllegalArgument_returns400() throws Exception {
        CreateExpenseRequestDTO req = new CreateExpenseRequestDTO(BigDecimal.ZERO, "", LocalDate.now());
        when(expenseService.create(any())).thenThrow(new IllegalArgumentException("Valor deve ser maior que zero"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_valid_returns200() throws Exception {
        CreateExpenseRequestDTO req = new CreateExpenseRequestDTO(new BigDecimal("120.00"), "Manutenção", LocalDate.now());
        ExpenseDTO dto = new ExpenseDTO(1L, req.valor(), req.categoria(), req.data(), LocalDateTime.now());
        when(expenseService.update(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoria").value("Manutenção"));
    }

    @Test
    void update_notFound_returns404() throws Exception {
        CreateExpenseRequestDTO req = new CreateExpenseRequestDTO(new BigDecimal("120.00"), "Manutenção", LocalDate.now());
        when(expenseService.update(eq(999L), any())).thenThrow(new EntityNotFoundException("não encontrada"));

        mockMvc.perform(put(BASE_URL + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_valid_returns204() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        org.mockito.Mockito.doThrow(new EntityNotFoundException("não encontrada"))
                .when(expenseService).delete(999L);

        mockMvc.perform(delete(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }
}
