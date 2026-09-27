package com.app.gerencia.services;

import com.app.gerencia.controllers.dto.record.ActivityRequestDTO;
import com.app.gerencia.entities.Activity;
import com.app.gerencia.entities.Professional;
import com.app.gerencia.repository.ActivityRepository;
import com.app.gerencia.repository.ProfessionalRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final ProfessionalRepository professionalRepository;

    public ActivityService(ActivityRepository activityRepository,
                           ProfessionalRepository professionalRepository) {
        this.activityRepository = activityRepository;
        this.professionalRepository = professionalRepository;
    }

    public Activity create(ActivityRequestDTO dto, Long professionalId) {
        Professional professional = professionalRepository.findById(professionalId)
                .orElseThrow(() -> new EntityNotFoundException("Profissional não encontrado"));

        Activity activity = new Activity();
        activity.setProfessional(professional);
        applyDto(activity, dto);
        return activityRepository.save(activity);
    }

    public Activity update(Long id, ActivityRequestDTO dto, Long requesterId, boolean isAdmin) {
        Activity activity = findById(id);
        assertOwnership(activity, requesterId, isAdmin);
        applyDto(activity, dto);
        activity.setUpdatedAt(LocalDateTime.now());
        return activityRepository.save(activity);
    }

    public Activity setActive(Long id, boolean active, Long requesterId, boolean isAdmin) {
        Activity activity = findById(id);
        assertOwnership(activity, requesterId, isAdmin);
        activity.setActive(active);
        activity.setUpdatedAt(LocalDateTime.now());
        return activityRepository.save(activity);
    }

    public Activity findById(Long id) {
        return activityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Atividade não encontrada com id: " + id));
    }

    // Profissional vê apenas suas atividades; admin tem visão total
    public List<Activity> findVisible(Long requesterId, boolean isAdmin) {
        return isAdmin
                ? activityRepository.findAllByOrderByNameAsc()
                : activityRepository.findByProfessionalIdOrderByNameAsc(requesterId);
    }

    public List<Activity> findActiveByProfessional(Long professionalId) {
        return activityRepository.findByProfessionalIdAndActiveTrueOrderByNameAsc(professionalId);
    }

    private void applyDto(Activity activity, ActivityRequestDTO dto) {
        if (dto.name() == null || dto.name().isBlank())
            throw new IllegalArgumentException("O nome da atividade é obrigatório");
        activity.setName(dto.name().trim());
        activity.setDescription(dto.description());
        activity.setCategory(dto.category());
    }

    private void assertOwnership(Activity activity, Long requesterId, boolean isAdmin) {
        if (isAdmin) return;
        if (!activity.getProfessional().getId().equals(requesterId)) {
            throw new AccessDeniedException("Você não tem permissão para alterar esta atividade");
        }
    }
}
