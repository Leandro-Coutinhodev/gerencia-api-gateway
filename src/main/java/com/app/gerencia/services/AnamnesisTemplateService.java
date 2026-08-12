package com.app.gerencia.services;

import com.app.gerencia.controllers.dto.AnamnesisTemplateRequestDTO;
import com.app.gerencia.entities.AnamnesisTemplate;
import com.app.gerencia.entities.AnamnesisTemplateField;
import com.app.gerencia.repository.AnamnesisTemplateFieldRepository;
import com.app.gerencia.repository.AnamnesisTemplateRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnamnesisTemplateService {

    private final AnamnesisTemplateRepository templateRepository;
    private final AnamnesisTemplateFieldRepository fieldRepository;

    public AnamnesisTemplateService(AnamnesisTemplateRepository templateRepository,
                                    AnamnesisTemplateFieldRepository fieldRepository) {
        this.templateRepository = templateRepository;
        this.fieldRepository = fieldRepository;
    }

    public AnamnesisTemplate save(AnamnesisTemplateRequestDTO dto) {
        AnamnesisTemplate template = new AnamnesisTemplate();
        template.setName(dto.name());
        template.setDescription(dto.description());
        template.setActive(true);
        template.setCreatedAt(LocalDateTime.now());

        List<AnamnesisTemplateField> fields = buildFields(dto.fields(), template);
        template.setFields(fields);

        return templateRepository.save(template);
    }

    public AnamnesisTemplate update(Long id, AnamnesisTemplateRequestDTO dto) {
        AnamnesisTemplate template = findById(id);
        template.setName(dto.name());
        template.setDescription(dto.description());


        Map<Long, AnamnesisTemplateField> existingById = template.getFields().stream()
                .filter(f -> f.getId() != null)
                .collect(Collectors.toMap(AnamnesisTemplateField::getId, f -> f));

        List<AnamnesisTemplateField> updatedFields = new ArrayList<>();

        for (AnamnesisTemplateRequestDTO.FieldRequestDTO fieldDTO : dto.fields()) {
            if (fieldDTO.id() != null && existingById.containsKey(fieldDTO.id())) {

                AnamnesisTemplateField existing = existingById.get(fieldDTO.id());
                applyFieldDTO(existing, fieldDTO, template);
                updatedFields.add(existing);
            } else {

                AnamnesisTemplateField newField = new AnamnesisTemplateField();
                applyFieldDTO(newField, fieldDTO, template);
                updatedFields.add(newField);
            }
        }

        // orphanRemoval = true
        template.getFields().clear();
        template.getFields().addAll(updatedFields);

        return templateRepository.save(template);
    }

    public AnamnesisTemplate findById(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Template não encontrado com id: " + id));
    }

    public AnamnesisTemplateField findFieldById(Long fieldId) {
        return fieldRepository.findById(fieldId)
                .orElseThrow(() -> new EntityNotFoundException("Campo não encontrado com id: " + fieldId));
    }

    public List<AnamnesisTemplate> findAllActive() {
        return templateRepository.findByActiveTrueOrderByCreatedAtDesc();
    }

    public List<AnamnesisTemplate> findAll() {
        return templateRepository.findAll();
    }

    // Soft delete: não remove o template pois anamneses antigas ainda o referenciam
    public void deactivate(Long id) {
        AnamnesisTemplate template = findById(id);
        template.setActive(false);
        templateRepository.save(template);
    }

    // Reativa um template desativado
    public AnamnesisTemplate reactivate(Long id) {
        AnamnesisTemplate template = findById(id);
        template.setActive(true);
        return templateRepository.save(template);
    }

    // --- Helpers privados ---

    private List<AnamnesisTemplateField> buildFields(
            List<AnamnesisTemplateRequestDTO.FieldRequestDTO> dtos,
            AnamnesisTemplate template) {

        List<AnamnesisTemplateField> fields = new ArrayList<>();
        for (AnamnesisTemplateRequestDTO.FieldRequestDTO dto : dtos) {
            AnamnesisTemplateField field = new AnamnesisTemplateField();
            applyFieldDTO(field, dto, template);
            fields.add(field);
        }
        return fields;
    }

    private void applyFieldDTO(AnamnesisTemplateField field,
                               AnamnesisTemplateRequestDTO.FieldRequestDTO dto,
                               AnamnesisTemplate template) {
        field.setTemplate(template);
        field.setLabel(dto.label());
        field.setFieldType(AnamnesisTemplateField.FieldType.valueOf(dto.fieldType().toUpperCase()));
        field.setRequired(dto.required());
        field.setPosition(dto.position());
        field.setPlaceholder(dto.placeholder());

        // Normaliza as opções do checkbox removendo espaços extras
        if (dto.options() != null && !dto.options().isBlank()) {
            String normalizedOptions = Arrays.stream(dto.options().split("\\|"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.joining("|"));
            field.setOptions(normalizedOptions);
        } else {
            field.setOptions(null);
        }
    }
}
