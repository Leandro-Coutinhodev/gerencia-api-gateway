package com.app.gerencia.controllers;

import com.app.gerencia.controllers.dto.DashboardDTO;
import com.app.gerencia.services.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// NOTA: addFilters = false desativa os filtros de segurança do Spring nesta suíte
// (mesmo padrão de FinanceDashboardControllerTests) — cobre roteamento/serialização,
// não a aplicação real do @PreAuthorize da classe.
@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTests {

    private static final String BASE_URL = "/api-gateway/gerencia/dashboard";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @Test
    void get_returns200ComDashboardCompleto() throws Exception {
        DashboardDTO dto = new DashboardDTO(
                new DashboardDTO.FinanceiroResumoDTO(new BigDecimal("1000.00"), new BigDecimal("300.00"), 3, 2, 1),
                new DashboardDTO.ContratosResumoDTO(2, List.of()),
                List.of(),
                new DashboardDTO.AnamneseResumoDTO(1, List.of())
        );
        when(dashboardService.generate()).thenReturn(dto);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.financeiro.saldoAcumulado").value(1000.00))
                .andExpect(jsonPath("$.contratos.aguardandoAssinaturaContagem").value(2))
                .andExpect(jsonPath("$.anamnese.pendentesContagem").value(1));
    }

    @Test
    void get_blocoFinanceiroNull_serializaNullSemQuebrar() throws Exception {
        DashboardDTO dto = new DashboardDTO(
                null,
                new DashboardDTO.ContratosResumoDTO(0, List.of()),
                List.of(),
                new DashboardDTO.AnamneseResumoDTO(0, List.of())
        );
        when(dashboardService.generate()).thenReturn(dto);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.financeiro").doesNotExist());
    }
}
