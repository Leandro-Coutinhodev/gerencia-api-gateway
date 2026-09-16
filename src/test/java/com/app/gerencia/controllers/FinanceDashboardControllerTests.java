package com.app.gerencia.controllers;

import com.app.gerencia.controllers.dto.FinanceDashboardDTO;
import com.app.gerencia.services.FinanceDashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FinanceDashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class FinanceDashboardControllerTests {

    private static final String BASE_URL = "/api-gateway/gerencia/financeiro/relatorio";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FinanceDashboardService financeDashboardService;

    private FinanceDashboardDTO sampleDto(String periodo) {
        return new FinanceDashboardDTO(periodo, new BigDecimal("400.00"), new BigDecimal("50.00"),
                new BigDecimal("1150.00"), new BigDecimal("43.75"), new BigDecimal("450.00"), 2,
                1, 1, 1, List.of());
    }

    @Test
    void get_comPeriodo_returns200() throws Exception {
        when(financeDashboardService.generate(eq("2026-08"))).thenReturn(sampleDto("2026-08"));

        mockMvc.perform(get(BASE_URL).param("periodo", "2026-08"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.periodo").value("2026-08"))
                .andExpect(jsonPath("$.saldoAcumulado").value(1150.00));
    }

    @Test
    void get_semPeriodo_delegaComNull() throws Exception {
        when(financeDashboardService.generate(any())).thenReturn(sampleDto("2026-09"));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.periodo").value("2026-09"));
    }

    @Test
    void get_periodoTudo_returns200() throws Exception {
        when(financeDashboardService.generate(eq("TUDO"))).thenReturn(sampleDto("TUDO"));

        mockMvc.perform(get(BASE_URL).param("periodo", "TUDO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.periodo").value("TUDO"));
    }

    @Test
    void get_periodoInvalido_returns400() throws Exception {
        when(financeDashboardService.generate(eq("xyz")))
                .thenThrow(new IllegalArgumentException("Período inválido: xyz"));

        mockMvc.perform(get(BASE_URL).param("periodo", "xyz"))
                .andExpect(status().isBadRequest());
    }
}
