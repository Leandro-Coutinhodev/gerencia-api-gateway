package com.app.gerencia.services;

import com.app.gerencia.controllers.dto.contract.*;
import com.app.gerencia.entities.*;
import com.app.gerencia.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ContractTemplateService {

    private final ContractTemplateRepository templateRepository;

    public ContractTemplateService(ContractTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }



    @Transactional
    public ContractTemplateDTO create(CreateContractTemplateRequestDTO req) {
        validate(req);

        if (templateRepository.findByName(req.name()).isPresent()) {
            throw new IllegalArgumentException("Já existe um modelo com o nome: " + req.name());
        }

        ContractTemplate template = new ContractTemplate();
        applyRequest(template, req);
        template = templateRepository.save(template);
        return ContractTemplateDTO.fromEntity(template);
    }

    @Transactional
    public ContractTemplateDTO update(Long id, CreateContractTemplateRequestDTO req) {
        ContractTemplate template = findEntityById(id);
        validate(req);

        templateRepository.findByName(req.name())
                .filter(t -> !t.getId().equals(id))
                .ifPresent(t -> { throw new IllegalArgumentException("Nome já utilizado por outro modelo"); });


        template.getClauses().clear();
        template.getVariables().clear();
        template.getAcceptFields().clear();

        applyRequest(template, req);
        template.setUpdatedAt(LocalDateTime.now());
        template = templateRepository.save(template);
        return ContractTemplateDTO.fromEntity(template);
    }

    public ContractTemplateDTO findById(Long id) {
        return ContractTemplateDTO.fromEntity(findEntityById(id));
    }

    public List<ContractTemplateDTO> findAllActive() {
        return templateRepository
                .findByStatusOrderByCreatedAtDesc(ContractTemplate.ContractTemplateStatus.ATIVO)
                .stream().map(ContractTemplateDTO::fromEntity).toList();
    }

    @Transactional
    public void deactivate(Long id) {
        ContractTemplate t = findEntityById(id);
        t.setStatus(ContractTemplate.ContractTemplateStatus.INATIVO);
        t.setUpdatedAt(LocalDateTime.now());
        templateRepository.save(t);
    }

    @Transactional
    public void reactivate(Long id) {
        ContractTemplate t = findEntityById(id);
        t.setStatus(ContractTemplate.ContractTemplateStatus.ATIVO);
        t.setUpdatedAt(LocalDateTime.now());
        templateRepository.save(t);
    }



    private void applyRequest(ContractTemplate template, CreateContractTemplateRequestDTO req) {
        template.setName(req.name().trim());
        template.setDescription(req.description());
        template.setType(ContractTemplate.ContractType.valueOf(req.type()));
        template.setSigningMode(ContractTemplate.SigningMode.valueOf(req.signingMode()));
        template.setWitnessConfig(ContractTemplate.WitnessConfig.valueOf(req.witnessConfig()));
        template.setWitnessCount(
                req.witnessCount() != null ? req.witnessCount() : 0);


        if (req.clauses() != null) {
            List<ContractClause> clauses = req.clauses().stream().map(c -> {
                ContractClause clause = new ContractClause();
                clause.setTemplate(template);
                clause.setClauseOrder(c.clauseOrder());
                clause.setTitle(c.title());
                clause.setContent(c.content());
                return clause;
            }).toList();
            if (template.getClauses() != null) template.getClauses().addAll(clauses);
            else template.setClauses(clauses);
        }


        if (req.variables() != null) {
            List<ContractVariable> variables = req.variables().stream().map(v -> {
                ContractVariable variable = new ContractVariable();
                variable.setTemplate(template);
                variable.setVariableName(v.variableName());
                variable.setDescription(v.description());
                variable.setType(ContractVariable.VariableType.valueOf(v.type()));
                variable.setRequired(v.required() != null ? v.required() : true);
                variable.setAutoFilled(v.autoFilled() != null ? v.autoFilled() : false);
                return variable;
            }).toList();
            if (template.getVariables() != null) template.getVariables().addAll(variables);
            else template.setVariables(variables);
        }


        if (req.acceptFields() != null) {
            List<ContractAcceptField> fields = req.acceptFields().stream().map(f -> {
                ContractAcceptField field = new ContractAcceptField();
                field.setTemplate(template);
                field.setFieldOrder(f.fieldOrder());
                field.setLabel(f.label());
                field.setFieldType(ContractAcceptField.AcceptFieldType.valueOf(f.fieldType()));
                field.setRequired(f.required() != null ? f.required() : true);
                return field;
            }).toList();
            if (template.getAcceptFields() != null) template.getAcceptFields().addAll(fields);
            else template.setAcceptFields(fields);
        }
    }

    private void validate(CreateContractTemplateRequestDTO req) {
        if (req.name() == null || req.name().isBlank())
            throw new IllegalArgumentException("Nome é obrigatório");
        if (req.type() == null)
            throw new IllegalArgumentException("Tipo é obrigatório");
        if (req.clauses() == null || req.clauses().isEmpty())
            throw new IllegalArgumentException("Ao menos uma cláusula é obrigatória");
    }

    ContractTemplate findEntityById(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Template não encontrado: " + id));
    }
}
