package com.app.gerencia.services;

import com.app.gerencia.controllers.dto.AnamnesisResponseSubmitDTO;
import com.app.gerencia.entities.Anamnesis;
import com.app.gerencia.entities.AnamnesisAnswer;
import com.app.gerencia.entities.AnamnesisTemplateField;
import com.app.gerencia.repository.AnamnesisAnswerRepository;
import com.app.gerencia.repository.AnamnesisRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import com.app.gerencia.utils.FileNaming;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AnamnesisAnswerService {

    private final AnamnesisAnswerRepository answerRepository;
    private final AnamnesisRepository anamnesisRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public AnamnesisAnswerService(AnamnesisAnswerRepository answerRepository, AnamnesisRepository anamnesisRepository) {
        this.answerRepository = answerRepository;
        this.anamnesisRepository = anamnesisRepository;
    }

    // Salva ou substitui todas as respostas de uma anamnese
    @Transactional
    public List<AnamnesisAnswer> saveAll(Anamnesis anamnesis,
                                         AnamnesisResponseSubmitDTO dto,
                                         List<MultipartFile> files) throws IOException {

        answerRepository.deleteAllByAnamnesisId(anamnesis.getId());
        entityManager.flush();

        List<AnamnesisAnswer> answers = new ArrayList<>();

        if (dto != null && dto.answers() != null) {
            for (AnamnesisResponseSubmitDTO.AnswerDTO answerDTO : dto.answers()) {
                answers.add(buildTextAnswer(anamnesis, answerDTO));
            }
        }


        if (files != null && !files.isEmpty()) {
            // Agrupa os arquivos por campo — um mesmo campo FILE pode ter vários PDFs
            // selecionados, que devem ser mesclados em um único arquivo salvo no banco.
            Map<Long, List<MultipartFile>> filesByField = new LinkedHashMap<>();
            for (MultipartFile file : files) {
                filesByField.computeIfAbsent(extractFieldId(file), k -> new ArrayList<>()).add(file);
            }
            for (Map.Entry<Long, List<MultipartFile>> entry : filesByField.entrySet()) {
                answers.add(buildFileAnswer(anamnesis, entry.getKey(), entry.getValue()));
            }
        }

        for (AnamnesisAnswer answer : answers) {
            entityManager.persist(answer);
        }

        entityManager.flush();

        return answers;
    }

    public List<AnamnesisAnswer> findByAnamnesisId(Long anamnesisId) {
        return answerRepository.findByAnamnesisId(anamnesisId);
    }

    public Optional<AnamnesisAnswer> findFileAnswer(Long anamnesisId, Long fieldId) {
        return answerRepository.findByAnamnesisIdAndFieldId(anamnesisId, fieldId)
                .stream()
                .filter(a -> a.getFileData() != null)
                .findFirst();
    }

    // --- Helpers privados ---

    private AnamnesisAnswer buildTextAnswer(Anamnesis anamnesis,
                                            AnamnesisResponseSubmitDTO.AnswerDTO dto) {
        AnamnesisAnswer answer = new AnamnesisAnswer();
        answer.setAnamnesis(anamnesis);

        // Busca o campo diretamente do template já carregado em memória
        AnamnesisTemplateField field = anamnesis.getTemplate().getFields().stream()
                .filter(f -> f.getId().equals(dto.fieldId()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Campo " + dto.fieldId() + " não pertence ao template desta anamnese"));

        validateFieldType(field, dto.value());

        answer.setField(field);
        answer.setValue(dto.value());
        return answer;
    }

    // Recebe todos os arquivos selecionados para um mesmo campo FILE. Quando há mais de um,
    // mescla todos em um único PDF antes de salvar (um campo = um arquivo no banco).
    private AnamnesisAnswer buildFileAnswer(Anamnesis anamnesis,
                                            Long fieldId,
                                            List<MultipartFile> files) throws IOException {
        AnamnesisTemplateField field = anamnesis.getTemplate().getFields().stream()
                .filter(f -> f.getId().equals(fieldId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Campo de arquivo " + fieldId + " não pertence ao template"));

        if (field.getFieldType() != AnamnesisTemplateField.FieldType.FILE) {
            throw new IllegalArgumentException(
                    "Campo " + field.getLabel() + " não é do tipo FILE");
        }

        for (MultipartFile file : files) {
            if (!isPdf(file)) {
                throw new IllegalArgumentException(
                        "Apenas arquivos PDF são aceitos no campo: " + field.getLabel());
            }
        }

        // Nome gerado (data + token aleatório) — nunca o nome original do arquivo, que pode
        // conter o nome da pessoa (ex: "cv_Leandro.pdf").
        byte[] fileData = files.size() == 1 ? files.get(0).getBytes() : mergePdfs(files);
        String fileName = FileNaming.generate("laudo", "pdf");

        AnamnesisAnswer answer = new AnamnesisAnswer();
        answer.setAnamnesis(anamnesis);
        answer.setField(field);
        answer.setFileData(fileData);
        answer.setFileName(fileName);
        return answer;
    }

    // Mescla vários PDFs em um único documento, na ordem em que foram selecionados
    private byte[] mergePdfs(List<MultipartFile> files) throws IOException {
        PDFMergerUtility merger = new PDFMergerUtility();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        merger.setDestinationStream(out);

        List<InputStream> streams = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                InputStream in = file.getInputStream();
                streams.add(in);
                merger.addSource(in);
            }
            merger.mergeDocuments(org.apache.pdfbox.io.MemoryUsageSetting.setupMainMemoryOnly());
        } finally {
            for (InputStream in : streams) {
                in.close();
            }
        }

        return out.toByteArray();
    }

    private void validateFieldType(AnamnesisTemplateField field, String value) {
        if (field.isRequired() && (value == null || value.isBlank())) {
            throw new IllegalArgumentException("Campo obrigatório não preenchido: " + field.getLabel());
        }

        if (value == null) return;

        switch (field.getFieldType()) {
            case DATE -> {
                try { LocalDate.parse(value); }
                catch (Exception e) {
                    throw new IllegalArgumentException("Data inválida no campo: " + field.getLabel());
                }
            }
            case CHECKBOX -> {
                if (field.getOptions() != null) {
                    List<String> valid = Arrays.asList(field.getOptions().split("\\|"));
                    List<String> selected = Arrays.asList(value.split("\\|"));
                    if (!valid.containsAll(selected)) {
                        throw new IllegalArgumentException(
                                "Opção inválida no campo: " + field.getLabel());
                    }
                }
            }
            case FILE -> throw new IllegalArgumentException(
                    "Campo FILE não deve vir em answers, use multipart files");
        }
    }

    private Long extractFieldId(MultipartFile file) {
        // Espera nome do campo multipart no formato "file_<fieldId>"
        String name = file.getName() != null ? file.getName() : "";
        try {
            return Long.parseLong(name.replace("file_", "").trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Nome do campo de arquivo inválido: '" + name + "'. Use o formato file_{fieldId}");
        }
    }

    private boolean isPdf(MultipartFile file) {
        String contentType = file.getContentType();
        String name = file.getOriginalFilename();
        return "application/pdf".equalsIgnoreCase(contentType)
                || (name != null && name.toLowerCase().endsWith(".pdf"));
    }
}