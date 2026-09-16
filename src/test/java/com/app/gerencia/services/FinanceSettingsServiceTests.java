package com.app.gerencia.services;

import com.app.gerencia.controllers.dto.FinanceSettingsDTO;
import com.app.gerencia.controllers.dto.UpdateFinanceSettingsRequestDTO;
import com.app.gerencia.entities.FinanceSettings;
import com.app.gerencia.repository.FinanceSettingsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinanceSettingsServiceTests {

    @Mock
    private FinanceSettingsRepository financeSettingsRepository;

    @InjectMocks
    private FinanceSettingsService financeSettingsService;

    @Test
    void get_noRowInDb_returnsDefaultFifteenWithoutSaving() {
        when(financeSettingsRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

        FinanceSettingsDTO result = financeSettingsService.get();

        assertThat(result.diasParaAtraso()).isEqualTo(15);
        verify(financeSettingsRepository, never()).save(any());
    }

    @Test
    void get_existingRow_returnsStoredValue() {
        FinanceSettings settings = new FinanceSettings();
        settings.setId(1L);
        settings.setDiasParaAtraso(20);
        when(financeSettingsRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(settings));

        FinanceSettingsDTO result = financeSettingsService.get();

        assertThat(result.diasParaAtraso()).isEqualTo(20);
    }

    @Test
    void update_validValue_persistsAndReturnsDto() {
        when(financeSettingsRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());
        when(financeSettingsRepository.save(any(FinanceSettings.class))).thenAnswer(inv -> {
            FinanceSettings s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });

        UpdateFinanceSettingsRequestDTO req = new UpdateFinanceSettingsRequestDTO(10);
        FinanceSettingsDTO result = financeSettingsService.update(req);

        assertThat(result.diasParaAtraso()).isEqualTo(10);
        ArgumentCaptor<FinanceSettings> captor = ArgumentCaptor.forClass(FinanceSettings.class);
        verify(financeSettingsRepository).save(captor.capture());
        assertThat(captor.getValue().getDiasParaAtraso()).isEqualTo(10);
    }

    @Test
    void update_existingRow_updatesSameEntity() {
        FinanceSettings existing = new FinanceSettings();
        existing.setId(1L);
        existing.setDiasParaAtraso(15);
        when(financeSettingsRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(existing));
        when(financeSettingsRepository.save(any(FinanceSettings.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateFinanceSettingsRequestDTO req = new UpdateFinanceSettingsRequestDTO(25);
        FinanceSettingsDTO result = financeSettingsService.update(req);

        assertThat(result.diasParaAtraso()).isEqualTo(25);
        ArgumentCaptor<FinanceSettings> captor = ArgumentCaptor.forClass(FinanceSettings.class);
        verify(financeSettingsRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(1L);
    }

    @Test
    void update_nullValue_throwsIllegalArgumentExceptionWithoutSaving() {
        UpdateFinanceSettingsRequestDTO req = new UpdateFinanceSettingsRequestDTO(null);

        assertThatThrownBy(() -> financeSettingsService.update(req))
                .isInstanceOf(IllegalArgumentException.class);
        verify(financeSettingsRepository, never()).save(any());
    }

    @Test
    void update_zeroValue_throwsIllegalArgumentExceptionWithoutSaving() {
        UpdateFinanceSettingsRequestDTO req = new UpdateFinanceSettingsRequestDTO(0);

        assertThatThrownBy(() -> financeSettingsService.update(req))
                .isInstanceOf(IllegalArgumentException.class);
        verify(financeSettingsRepository, never()).save(any());
    }

    @Test
    void update_negativeValue_throwsIllegalArgumentExceptionWithoutSaving() {
        UpdateFinanceSettingsRequestDTO req = new UpdateFinanceSettingsRequestDTO(-5);

        assertThatThrownBy(() -> financeSettingsService.update(req))
                .isInstanceOf(IllegalArgumentException.class);
        verify(financeSettingsRepository, never()).save(any());
    }

    @Test
    void getDiasParaAtrasoValue_noRowInDb_returnsDefaultFifteen() {
        when(financeSettingsRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

        int result = financeSettingsService.getDiasParaAtrasoValue();

        assertThat(result).isEqualTo(15);
    }

    @Test
    void getDiasParaAtrasoValue_existingRow_returnsStoredValue() {
        FinanceSettings settings = new FinanceSettings();
        settings.setId(1L);
        settings.setDiasParaAtraso(7);
        when(financeSettingsRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(settings));

        int result = financeSettingsService.getDiasParaAtrasoValue();

        assertThat(result).isEqualTo(7);
    }
}
