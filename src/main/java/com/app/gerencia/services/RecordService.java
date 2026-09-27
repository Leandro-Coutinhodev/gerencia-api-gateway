package com.app.gerencia.services;

import com.app.gerencia.controllers.dto.record.CreateRecordRequestDTO;
import com.app.gerencia.controllers.dto.record.RecordAnswers;
import com.app.gerencia.entities.*;
import com.app.gerencia.repository.PatientRepository;
import com.app.gerencia.repository.ProfessionalRepository;
import com.app.gerencia.repository.RecordRepository;
import com.app.gerencia.repository.RecordTemplateRepository;
import com.google.gson.Gson;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecordService {

    private final RecordRepository recordRepository;
    private final RecordTemplateRepository templateRepository;
    private final PatientRepository patientRepository;
    private final ProfessionalRepository professionalRepository;
    private final Gson gson = new Gson();

    public RecordService(RecordRepository recordRepository,
                         RecordTemplateRepository templateRepository,
                         PatientRepository patientRepository,
                         ProfessionalRepository professionalRepository) {
        this.recordRepository = recordRepository;
        this.templateRepository = templateRepository;
        this.patientRepository = patientRepository;
        this.professionalRepository = professionalRepository;
    }

    @Transactional
    public AttendanceRecord create(CreateRecordRequestDTO dto, Long professionalId) {
        if (dto.sessionDate() == null)
            throw new IllegalArgumentException("A data da sessão é obrigatória");

        RecordTemplate template = templateRepository.findById(dto.templateId())
                .orElseThrow(() -> new EntityNotFoundException("Modelo não encontrado"));
        if (!template.getProfessional().getId().equals(professionalId)) {
            throw new AccessDeniedException("Este modelo pertence a outro profissional");
        }

        Patient patient = patientRepository.findById(dto.patientId())
                .orElseThrow(() -> new EntityNotFoundException("Paciente não encontrado"));
        Professional professional = professionalRepository.findById(professionalId)
                .orElseThrow(() -> new EntityNotFoundException("Profissional não encontrado"));

        RecordAnswers answers = new RecordAnswers();
        if (dto.generalAnswers() != null) {
            answers.setGeneral(new LinkedHashMap<>(dto.generalAnswers()));
        }
        if (dto.activityAnswers() != null) {
            answers.setActivities(new LinkedHashMap<>(dto.activityAnswers()));
        }

        validateRequiredFields(template, answers);

        AttendanceRecord record = new AttendanceRecord();
        record.setTemplate(template);
        record.setPatient(patient);
        record.setProfessional(professional);
        record.setSessionDate(dto.sessionDate());
        record.setAnswersData(gson.toJson(answers));

        return recordRepository.save(record);
    }

    public AttendanceRecord findById(Long id) {
        return recordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ficha não encontrada com id: " + id));
    }

    public List<AttendanceRecord> findByPatient(Long patientId) {
        if (!patientRepository.existsById(patientId))
            throw new EntityNotFoundException("Paciente não encontrado com id: " + patientId);
        return recordRepository.findByPatientIdOrderBySessionDateDesc(patientId);
    }

    public List<AttendanceRecord> findAll() {
        return recordRepository.findAllByOrderByCreatedAtDesc();
    }

    public RecordAnswers parseAnswers(AttendanceRecord record) {
        if (record.getAnswersData() == null) return new RecordAnswers();
        RecordAnswers parsed = gson.fromJson(record.getAnswersData(), RecordAnswers.class);
        return parsed != null ? parsed : new RecordAnswers();
    }

    // --- Helpers privados ---

    private void validateRequiredFields(RecordTemplate template, RecordAnswers answers) {
        for (RecordTemplateGeneralField f : template.getGeneralFields()) {
            if (!f.isRequired()) continue;
            String value = answers.getGeneral().get(String.valueOf(f.getId()));
            if (value == null || value.isBlank())
                throw new IllegalArgumentException("O campo \"" + f.getLabel() + "\" é obrigatório");
        }

        for (RecordTemplateActivity item : template.getActivityItems()) {
            Map<String, String> itemAnswers = answers.getActivities()
                    .getOrDefault(String.valueOf(item.getId()), Map.of());
            for (RecordTemplateActivityField f : item.getFields()) {
                if (!f.isRequired()) continue;
                String value = itemAnswers.get(String.valueOf(f.getId()));
                if (value == null || value.isBlank())
                    throw new IllegalArgumentException(
                            "O campo \"" + f.getLabel() + "\" (" + activityLabel(item) + ") é obrigatório");
            }
        }
    }

    private String activityLabel(RecordTemplateActivity item) {
        return item.getLabel() != null && !item.getLabel().isBlank()
                ? item.getLabel() : item.getActivity().getName();
    }
}
