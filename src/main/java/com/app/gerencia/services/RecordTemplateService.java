package com.app.gerencia.services;

import com.app.gerencia.controllers.dto.record.RecordTemplateRequestDTO;
import com.app.gerencia.entities.*;
import com.app.gerencia.enums.RecordFieldType;
import com.app.gerencia.repository.ActivityRepository;
import com.app.gerencia.repository.ProfessionalRepository;
import com.app.gerencia.repository.RecordTemplateRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RecordTemplateService {

    private final RecordTemplateRepository templateRepository;
    private final ActivityRepository activityRepository;
    private final ProfessionalRepository professionalRepository;

    public RecordTemplateService(RecordTemplateRepository templateRepository,
                                 ActivityRepository activityRepository,
                                 ProfessionalRepository professionalRepository) {
        this.templateRepository = templateRepository;
        this.activityRepository = activityRepository;
        this.professionalRepository = professionalRepository;
    }

    public RecordTemplate create(RecordTemplateRequestDTO dto, Long professionalId) {
        Professional professional = professionalRepository.findById(professionalId)
                .orElseThrow(() -> new EntityNotFoundException("Profissional não encontrado"));

        RecordTemplate template = new RecordTemplate();
        template.setProfessional(professional);
        applyDto(template, dto, professionalId);
        return templateRepository.save(template);
    }

    public RecordTemplate update(Long id, RecordTemplateRequestDTO dto, Long requesterId, boolean isAdmin) {
        RecordTemplate template = findById(id);
        assertOwnership(template, requesterId, isAdmin);
        applyDto(template, dto, template.getProfessional().getId());
        template.setUpdatedAt(LocalDateTime.now());
        return templateRepository.save(template);
    }

    public RecordTemplate setActive(Long id, boolean active, Long requesterId, boolean isAdmin) {
        RecordTemplate template = findById(id);
        assertOwnership(template, requesterId, isAdmin);
        template.setActive(active);
        template.setUpdatedAt(LocalDateTime.now());
        return templateRepository.save(template);
    }

    public RecordTemplate findById(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Modelo não encontrado com id: " + id));
    }

    public List<RecordTemplate> findVisible(Long requesterId, boolean isAdmin) {
        return isAdmin
                ? templateRepository.findAllByOrderByCreatedAtDesc()
                : templateRepository.findByProfessionalIdOrderByCreatedAtDesc(requesterId);
    }

    public List<RecordTemplate> findActiveByProfessional(Long professionalId) {
        return templateRepository.findByProfessionalIdAndActiveTrueOrderByCreatedAtDesc(professionalId);
    }

    // --- Helpers privados ---

    private void applyDto(RecordTemplate template, RecordTemplateRequestDTO dto, Long ownerProfessionalId) {
        if (dto.name() == null || dto.name().isBlank())
            throw new IllegalArgumentException("O nome do modelo é obrigatório");
        template.setName(dto.name().trim());
        template.setDescription(dto.description());

        applyGeneralFields(template, dto.generalFields());
        applyActivityItems(template, dto.activityItems(), ownerProfessionalId);
    }

    private void applyGeneralFields(RecordTemplate template, List<RecordTemplateRequestDTO.GeneralFieldRequestDTO> dtos) {
        Map<Long, RecordTemplateGeneralField> existingById = template.getGeneralFields().stream()
                .filter(f -> f.getId() != null)
                .collect(Collectors.toMap(RecordTemplateGeneralField::getId, f -> f));

        List<RecordTemplateGeneralField> updated = new ArrayList<>();
        if (dtos != null) {
            for (var fieldDto : dtos) {
                RecordTemplateGeneralField field = fieldDto.id() != null && existingById.containsKey(fieldDto.id())
                        ? existingById.get(fieldDto.id())
                        : new RecordTemplateGeneralField();
                field.setTemplate(template);
                applyFieldCommon(field::setLabel, field::setFieldType, field::setUnit, field::setRequired,
                        field::setPosition, field::setOptions, fieldDto.label(), fieldDto.fieldType(),
                        fieldDto.unit(), fieldDto.required(), fieldDto.position(), fieldDto.options());
                updated.add(field);
            }
        }

        // orphanRemoval = true
        template.getGeneralFields().clear();
        template.getGeneralFields().addAll(updated);
    }

    private void applyActivityItems(RecordTemplate template, List<RecordTemplateRequestDTO.ActivityItemRequestDTO> dtos,
                                    Long ownerProfessionalId) {
        Map<Long, RecordTemplateActivity> existingById = template.getActivityItems().stream()
                .filter(a -> a.getId() != null)
                .collect(Collectors.toMap(RecordTemplateActivity::getId, a -> a));

        List<RecordTemplateActivity> updated = new ArrayList<>();
        if (dtos != null) {
            for (var itemDto : dtos) {
                Activity activity = activityRepository.findById(itemDto.activityId())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Atividade não encontrada: " + itemDto.activityId()));
                if (!activity.getProfessional().getId().equals(ownerProfessionalId)) {
                    throw new IllegalArgumentException(
                            "A atividade \"" + activity.getName() + "\" não pertence a este profissional");
                }

                RecordTemplateActivity item = itemDto.id() != null && existingById.containsKey(itemDto.id())
                        ? existingById.get(itemDto.id())
                        : new RecordTemplateActivity();
                item.setTemplate(template);
                item.setActivity(activity);
                item.setLabel(itemDto.label());
                item.setPosition(itemDto.position());

                applyActivityFields(item, itemDto.fields());
                updated.add(item);
            }
        }

        // orphanRemoval = true
        template.getActivityItems().clear();
        template.getActivityItems().addAll(updated);
    }

    private void applyActivityFields(RecordTemplateActivity item, List<RecordTemplateRequestDTO.FieldRequestDTO> dtos) {
        Map<Long, RecordTemplateActivityField> existingById = item.getFields().stream()
                .filter(f -> f.getId() != null)
                .collect(Collectors.toMap(RecordTemplateActivityField::getId, f -> f));

        List<RecordTemplateActivityField> updated = new ArrayList<>();
        if (dtos != null) {
            for (var fieldDto : dtos) {
                RecordTemplateActivityField field = fieldDto.id() != null && existingById.containsKey(fieldDto.id())
                        ? existingById.get(fieldDto.id())
                        : new RecordTemplateActivityField();
                field.setTemplateActivity(item);
                applyFieldCommon(field::setLabel, field::setFieldType, field::setUnit, field::setRequired,
                        field::setPosition, field::setOptions, fieldDto.label(), fieldDto.fieldType(),
                        fieldDto.unit(), fieldDto.required(), fieldDto.position(), fieldDto.options());
                updated.add(field);
            }
        }

        item.getFields().clear();
        item.getFields().addAll(updated);
    }

    // Aplica os atributos comuns de campo (label, tipo, unidade, obrigatoriedade, posição, opções)
    // tanto para RecordTemplateGeneralField quanto RecordTemplateActivityField, via setters passados por referência.
    private void applyFieldCommon(
            java.util.function.Consumer<String> setLabel,
            java.util.function.Consumer<RecordFieldType> setFieldType,
            java.util.function.Consumer<String> setUnit,
            java.util.function.Consumer<Boolean> setRequired,
            java.util.function.Consumer<Integer> setPosition,
            java.util.function.Consumer<String> setOptions,
            String label, String fieldType, String unit, boolean required, int position, String options
    ) {
        if (label == null || label.isBlank())
            throw new IllegalArgumentException("Todo campo precisa de um nome");

        setLabel.accept(label.trim());
        setFieldType.accept(RecordFieldType.valueOf(fieldType.toUpperCase()));
        setUnit.accept(unit != null && !unit.isBlank() ? unit.trim() : null);
        setRequired.accept(required);
        setPosition.accept(position);

        if (options != null && !options.isBlank()) {
            String normalized = Arrays.stream(options.split("\\|"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.joining("|"));
            setOptions.accept(normalized);
        } else {
            setOptions.accept(null);
        }
    }

    private void assertOwnership(RecordTemplate template, Long requesterId, boolean isAdmin) {
        if (isAdmin) return;
        if (!template.getProfessional().getId().equals(requesterId)) {
            throw new AccessDeniedException("Você não tem permissão para alterar este modelo");
        }
    }
}
