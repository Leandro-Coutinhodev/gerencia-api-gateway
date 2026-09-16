package com.app.gerencia.controllers;

import com.app.gerencia.controllers.dto.FinanceSettingsDTO;
import com.app.gerencia.controllers.dto.UpdateFinanceSettingsRequestDTO;
import com.app.gerencia.services.FinanceSettingsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FinanceSettingsController.class)
@AutoConfigureMockMvc(addFilters = false)
class FinanceSettingsControllerTests {

    private static final String BASE_URL = "/api-gateway/gerencia/financeiro/configuracao";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FinanceSettingsService financeSettingsService;

    @Test
    void get_returns200WithValueFromService() throws Exception {
        when(financeSettingsService.get()).thenReturn(new FinanceSettingsDTO(15));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diasParaAtraso").value(15));
    }

    @Test
    void put_valid_returns200WithUpdatedValue() throws Exception {
        UpdateFinanceSettingsRequestDTO req = new UpdateFinanceSettingsRequestDTO(10);
        when(financeSettingsService.update(any())).thenReturn(new FinanceSettingsDTO(10));

        mockMvc.perform(put(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diasParaAtraso").value(10));
    }

    @Test
    void put_serviceThrowsIllegalArgument_returns400WithMessage() throws Exception {
        UpdateFinanceSettingsRequestDTO req = new UpdateFinanceSettingsRequestDTO(0);
        when(financeSettingsService.update(any()))
                .thenThrow(new IllegalArgumentException("Dias para atraso deve ser um número inteiro positivo"));

        mockMvc.perform(put(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Dias para atraso deve ser um número inteiro positivo"));
    }
}
