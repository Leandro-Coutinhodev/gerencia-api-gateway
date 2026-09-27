package com.app.gerencia.services;

import com.app.gerencia.controllers.dto.appointment.UpdateFrequencyStatusRequestDTO;
import com.app.gerencia.entities.Frequency;
import com.app.gerencia.enums.FrequencyStatus;
import com.app.gerencia.repository.FrequencyRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

// Registro de presença/ausência de cada ocorrência (Frequency). O histórico nunca é apagado
// (RB09) — apenas o status e a observação são atualizados.
@Service
public class FrequencyService {

    private final FrequencyRepository frequencyRepository;

    public FrequencyService(FrequencyRepository frequencyRepository) {
        this.frequencyRepository = frequencyRepository;
    }

    public Frequency findById(Long id, Long requesterId, boolean isAdmin) {
        Frequency f = frequencyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Frequência não encontrada com id: " + id));
        assertOwnership(f, requesterId, isAdmin);
        return f;
    }

    public Frequency updateStatus(Long id, UpdateFrequencyStatusRequestDTO dto, Long requesterId, boolean isAdmin) {
        Frequency f = frequencyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Frequência não encontrada com id: " + id));
        assertOwnership(f, requesterId, isAdmin);

        if (dto.status() == null || dto.status().isBlank())
            throw new IllegalArgumentException("Informe o status da frequência");

        FrequencyStatus status;
        try {
            status = FrequencyStatus.valueOf(dto.status().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status inválido: " + dto.status());
        }

        f.setStatus(status);
        f.setObservation(dto.observation());
        f.setMarkedAt(LocalDateTime.now());
        f.setUpdatedAt(LocalDateTime.now());
        return frequencyRepository.save(f);
    }

    private void assertOwnership(Frequency f, Long requesterId, boolean isAdmin) {
        if (isAdmin) return;
        if (!f.getProfessional().getId().equals(requesterId)) {
            throw new AccessDeniedException("Você não tem permissão para alterar esta frequência");
        }
    }
}
