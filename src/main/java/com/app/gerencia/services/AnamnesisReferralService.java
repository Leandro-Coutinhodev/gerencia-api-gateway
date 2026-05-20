package com.app.gerencia.services;

import com.app.gerencia.controllers.dto.AnamnesisReferralRequestDTO;
import com.app.gerencia.entities.*;
import com.app.gerencia.repository.*;
import com.app.gerencia.utils.PdfGenerator;
import com.nimbusds.jose.shaded.gson.Gson;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.*;

@Service
public class AnamnesisReferralService {

    private final AnamnesisReferralRepository referralRepository;
    private final AnamnesisRepository anamnesisRepository;
    private final AnamnesisAnswerRepository answerRepository;
    private final AssistantRepository assistantRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public AnamnesisReferralService(AnamnesisReferralRepository referralRepository,
                                    AnamnesisRepository anamnesisRepository,
                                    AnamnesisAnswerRepository answerRepository,
                                    AssistantRepository assistantRepository,
                                    UserRepository userRepository,
                                    EmailService emailService) {
        this.referralRepository = referralRepository;
        this.anamnesisRepository = anamnesisRepository;
        this.answerRepository = answerRepository;
        this.assistantRepository = assistantRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    // ── Criação do encaminhamento ─────────────────────────────────────────────

    @Transactional
    public AnamnesisReferral createReferral(Long senderId,
                                            AnamnesisReferralRequestDTO request) {

        if (request.anamnesisId() == null)
            throw new IllegalArgumentException("anamnesisId não pode ser nulo");
        if (senderId == null)
            throw new IllegalArgumentException("senderId não pode ser nulo");

        Anamnesis anamnesis = anamnesisRepository.findById(request.anamnesisId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Anamnese não encontrada: " + request.anamnesisId()));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Profissional remetente não encontrado: " + senderId));

        Assistant receiver = null;
        if (request.assistantId() != null) {
            receiver = assistantRepository.findById(request.assistantId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Assistente não encontrado: " + request.assistantId()));
        }

        // Busca apenas as respostas dos campos selecionados
        List<AnamnesisAnswer> selectedAnswers = answerRepository
                .findByAnamnesisId(anamnesis.getId())
                .stream()
                .filter(a -> request.selectedFieldIds().contains(a.getField().getId()))
                .toList();

        // Monta JSON: [{ "label": "...", "value": "...", "fieldType": "..." }]
        String selectedFieldsJson = buildSelectedJson(selectedAnswers);

        AnamnesisReferral referral = new AnamnesisReferral();
        referral.setAnamnesis(anamnesis);
        referral.setSender(sender);
        referral.setAssistant(receiver);
        referral.setSelectedFieldsJson(selectedFieldsJson);
        referral.setSentAt(new Date());

        anamnesis.setStatus('P'); // Em análise
        anamnesisRepository.save(anamnesis);

        return referralRepository.save(referral);
    }

    // ── Atribuição de assistente (sem e-mail) ─────────────────────────────────

    @Transactional
    public AnamnesisReferral assignAssistant(Long referralId, Long assistantId) {
        AnamnesisReferral referral = findById(referralId);
        Assistant assistant = assistantRepository.findById(assistantId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Assistente não encontrado: " + assistantId));
        referral.setAssistant(assistant);
        return referralRepository.save(referral);
    }

    // ── Atribuição de assistente (com e-mail + PDF) ───────────────────────────

    @Transactional
    public AnamnesisReferral assignAssistantEmail(Long referralId, Long assistantId) {
        AnamnesisReferral referral = findById(referralId);
        Assistant assistant = assistantRepository.findById(assistantId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Assistente não encontrado: " + assistantId));

        referral.setAssistant(assistant);
        AnamnesisReferral saved = referralRepository.save(referral);

        if (assistant.getEmail() != null) {
            Patient patient = referral.getAnamnesis().getPatient();

            byte[] pdfBytes = PdfGenerator.generateReferralPdf(
                    referral.getSelectedFieldsJson(),
                    patient.getName(),
                    patient.getCpf(),
                    referral.getSentAt(),
                    null // report por campo agora: pode buscar se necessário
            );

            String subject = "Nova Anamnese Encaminhada";
            String body = String.format(
                    "Olá %s,\n\n" +
                            "Você foi vinculado(a) a uma nova anamnese referente ao(a) paciente %s.\n\n" +
                            "O relatório em anexo contém as informações selecionadas.\n\n" +
                            "Data do encaminhamento: %s\n\n" +
                            "Atenciosamente,\nEquipe GerenciA",
                    assistant.getName(),
                    patient.getName(),
                    saved.getSentAt() != null ? saved.getSentAt().toString() : "—"
            );

            emailService.sendEmailWithAttachment(
                    assistant.getEmail(), subject, body,
                    pdfBytes,
                    "relatorio-anamnese-" + saved.getId() + ".pdf"
            );
        }

        return saved;
    }

    // ── Leitura ───────────────────────────────────────────────────────────────

    public AnamnesisReferral findById(Long id) {
        return referralRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Encaminhamento não encontrado: " + id));
    }

    public List<AnamnesisReferral> findAll() {
        return referralRepository.findAllByAssistantIdIsNotNull()
                .orElse(Collections.emptyList());
    }

    public List<AnamnesisReferral> findByAssistantId(Long id) {
        return referralRepository.findAllByAssistantIdIsNotNullAndAssistantId(id);
    }

    // ── Helper: monta JSON das respostas selecionadas ─────────────────────────

    private String buildSelectedJson(List<AnamnesisAnswer> answers) {
        // Formato: [{ label, value, fieldType }]
        // Campos FILE são incluídos apenas como indicador (sem o binário)
        var list = answers.stream()
                .sorted(Comparator.comparingInt(a -> a.getField().getPosition()))
                .map(a -> {
                    Map<String, String> entry = new LinkedHashMap<>();
                    entry.put("label", a.getField().getLabel());
                    entry.put("fieldType", a.getField().getFieldType().name());

                    if (a.getField().getFieldType() == AnamnesisTemplateField.FieldType.FILE) {
                        entry.put("value", a.getFileData() != null
                                ? "[Arquivo: " + a.getFileName() + "]"
                                : "[Sem arquivo]");
                    } else {
                        entry.put("value", a.getValue() != null ? a.getValue() : "");
                    }

                    return entry;
                })
                .toList();

        return new com.google.gson.Gson().toJson(list);
    }
}
