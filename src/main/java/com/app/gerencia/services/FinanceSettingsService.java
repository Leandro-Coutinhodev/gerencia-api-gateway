package com.app.gerencia.services;

import com.app.gerencia.controllers.dto.FinanceSettingsDTO;
import com.app.gerencia.controllers.dto.UpdateFinanceSettingsRequestDTO;
import com.app.gerencia.entities.FinanceSettings;
import com.app.gerencia.repository.FinanceSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinanceSettingsService {

    private static final int DEFAULT_DIAS_PARA_ATRASO = 15;

    private final FinanceSettingsRepository financeSettingsRepository;

    public FinanceSettingsService(FinanceSettingsRepository financeSettingsRepository) {
        this.financeSettingsRepository = financeSettingsRepository;
    }

    public FinanceSettingsDTO get() {
        return new FinanceSettingsDTO(getDiasParaAtrasoValue());
    }

    @Transactional
    public FinanceSettingsDTO update(UpdateFinanceSettingsRequestDTO req) {
        if (req.diasParaAtraso() == null || req.diasParaAtraso() <= 0) {
            throw new IllegalArgumentException("Dias para atraso deve ser um número inteiro positivo");
        }

        FinanceSettings settings = financeSettingsRepository.findFirstByOrderByIdAsc()
                .orElseGet(FinanceSettings::new);
        settings.setDiasParaAtraso(req.diasParaAtraso());
        settings = financeSettingsRepository.save(settings);

        return new FinanceSettingsDTO(settings.getDiasParaAtraso());
    }

    public int getDiasParaAtrasoValue() {
        return financeSettingsRepository.findFirstByOrderByIdAsc()
                .map(FinanceSettings::getDiasParaAtraso)
                .orElse(DEFAULT_DIAS_PARA_ATRASO);
    }
}
