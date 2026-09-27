package com.app.gerencia.controllers;

import com.app.gerencia.controllers.dto.ChargeDTO;
import com.app.gerencia.controllers.dto.CreateChargeRequestDTO;
import com.app.gerencia.controllers.dto.RegisterPaymentRequestDTO;
import com.app.gerencia.entities.Charge;
import com.app.gerencia.services.ChargeService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChargeController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChargeControllerTests {

    private static final String BASE_URL = "/api-gateway/gerencia/financeiro/cobranca";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChargeService chargeService;

    private ChargeDTO sampleDto() {
        return new ChargeDTO(1L, 1L, "João da Silva", "Maria da Silva",
                new BigDecimal("250.00"), null, Charge.PaymentMethod.PIX,
                LocalDate.of(2026, 9, 20), Charge.ChargeStatus.PENDENTE, null, LocalDateTime.now(),
                false, 0);
    }

    @Test
    void getAll_returnsList() throws Exception {
        when(chargeService.findAll(null)).thenReturn(List.of(sampleDto()));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patientName").value("João da Silva"));
    }

    @Test
    void create_valid_returns201() throws Exception {
        CreateChargeRequestDTO req = new CreateChargeRequestDTO(
                1L, new BigDecimal("250.00"), null, "PIX", LocalDate.of(2026, 9, 20));
        when(chargeService.create(any())).thenReturn(sampleDto());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void create_serviceThrowsIllegalArgument_returns400() throws Exception {
        CreateChargeRequestDTO req = new CreateChargeRequestDTO(null, BigDecimal.ZERO, null, "PIX", LocalDate.now());
        when(chargeService.create(any())).thenThrow(new IllegalArgumentException("Valor deve ser maior que zero"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_valid_returns200() throws Exception {
        CreateChargeRequestDTO req = new CreateChargeRequestDTO(
                1L, new BigDecimal("300.00"), null, "PIX", LocalDate.of(2026, 9, 25));
        when(chargeService.update(eq(1L), any())).thenReturn(sampleDto());

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void update_chargePaga_returns400() throws Exception {
        CreateChargeRequestDTO req = new CreateChargeRequestDTO(
                1L, new BigDecimal("300.00"), null, "PIX", LocalDate.of(2026, 9, 25));
        when(chargeService.update(eq(1L), any()))
                .thenThrow(new IllegalStateException("Cobrança já paga não pode ser editada"));

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_notFound_returns404() throws Exception {
        CreateChargeRequestDTO req = new CreateChargeRequestDTO(
                1L, new BigDecimal("300.00"), null, "PIX", LocalDate.of(2026, 9, 25));
        when(chargeService.update(eq(999L), any())).thenThrow(new EntityNotFoundException("não encontrada"));

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
    void delete_chargePaga_returns400() throws Exception {
        org.mockito.Mockito.doThrow(new IllegalStateException("Cobrança já paga não pode ser excluída"))
                .when(chargeService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        org.mockito.Mockito.doThrow(new EntityNotFoundException("não encontrada"))
                .when(chargeService).delete(999L);

        mockMvc.perform(delete(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void registerPayment_valid_returns200() throws Exception {
        RegisterPaymentRequestDTO req = new RegisterPaymentRequestDTO("PIX", LocalDate.of(2026, 8, 25));
        when(chargeService.registerPayment(eq(1L), any())).thenReturn(sampleDto());

        mockMvc.perform(put(BASE_URL + "/1/baixa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void registerPayment_notFound_returns404() throws Exception {
        RegisterPaymentRequestDTO req = new RegisterPaymentRequestDTO("PIX", LocalDate.now());
        when(chargeService.registerPayment(eq(999L), any()))
                .thenThrow(new EntityNotFoundException("não encontrada"));

        mockMvc.perform(put(BASE_URL + "/999/baixa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getRecibo_paid_returnsPdf() throws Exception {
        byte[] pdfBytes = "fake-pdf".getBytes();
        when(chargeService.generateReceipt(1L)).thenReturn(pdfBytes);

        mockMvc.perform(get(BASE_URL + "/1/recibo"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    @Test
    void getRecibo_notPaid_returns400() throws Exception {
        when(chargeService.generateReceipt(2L)).thenThrow(new IllegalStateException("Cobrança ainda não foi paga"));

        mockMvc.perform(get(BASE_URL + "/2/recibo"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRecibo_notFound_returns404() throws Exception {
        when(chargeService.generateReceipt(3L)).thenThrow(new EntityNotFoundException("não encontrada"));

        mockMvc.perform(get(BASE_URL + "/3/recibo"))
                .andExpect(status().isNotFound());
    }
}
