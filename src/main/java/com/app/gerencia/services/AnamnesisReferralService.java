package com.app.gerencia.services;

import com.app.gerencia.controllers.dto.AnamnesisReferralRequestDTO;
import com.app.gerencia.entities.Anamnesis;
import com.app.gerencia.entities.AnamnesisReferral;
import com.app.gerencia.entities.Assistant;
import com.app.gerencia.entities.Professional;
import com.app.gerencia.repository.AnamnesisReferralRepository;
import com.app.gerencia.repository.AnamnesisRepository;
import com.app.gerencia.repository.AssistantRepository;
import com.app.gerencia.repository.ProfessionalRepository;
import com.nimbusds.jose.shaded.gson.Gson;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class AnamnesisReferralService{

    private AnamnesisReferralRepository referralRepository;

    private AnamnesisRepository anamnesisRepository;

    private AssistantRepository assistantRepository;

    private ProfessionalRepository professionalRepository;

    public AnamnesisReferralService(AnamnesisReferralRepository anamnesisReferralRepository,
                                    AnamnesisRepository anamnesisRepository,
                                    AssistantRepository assistantRepository,
                                    ProfessionalRepository professionalRepository){
        referralRepository = anamnesisReferralRepository;
        this.anamnesisRepository = anamnesisRepository;
        this.assistantRepository = assistantRepository;
        this.professionalRepository = professionalRepository;
    }

    public AnamnesisReferral createReferral(Long senderId, AnamnesisReferralRequestDTO request) {

        // VALIDAÇÕES CRÍTICAS (apenas anamnesisId é obrigatório)
        if (request.anamnesisId() == null) {
            throw new IllegalArgumentException("Anamnesis ID não pode ser nulo");
        }

        if (senderId == null) {
            throw new IllegalArgumentException("Sender ID não pode ser nulo");
        }

        // DEBUG
        System.out.println("=== DEBUG CREATE REFERRAL ===");
        System.out.println("Sender ID: " + senderId);
        System.out.println("Anamnesis ID: " + request.anamnesisId());
        System.out.println("Assistant ID: " + request.assistantId());
        System.out.println("Selected Fields: " + request.selectedFields());

        Anamnesis anamnesis = anamnesisRepository.findById(request.anamnesisId())
                .orElseThrow(() -> new RuntimeException("Anamnese não encontrada com ID: " + request.anamnesisId()));

        Professional sender = professionalRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Profissional remetente não encontrado com ID: " + senderId));

        // Assistant é opcional - busca apenas se o ID foi fornecido
        Assistant receiver = null;
        if (request.assistantId() != null) {
            receiver = assistantRepository.findById(request.assistantId())
                    .orElseThrow(() -> new RuntimeException("Profissional destinatário não encontrado com ID: " + request.assistantId()));
        }

        // Monta o JSON com os campos selecionados
        Map<String, Object> selectedData = new HashMap<>();
        for (String field : request.selectedFields()) {
            try {
                Field f = Anamnesis.class.getDeclaredField(field);
                f.setAccessible(true);
                selectedData.put(field, f.get(anamnesis));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Campo inválido: " + field);
            }
        }

        String selectedFieldsJson = new Gson().toJson(selectedData);

        AnamnesisReferral referral = new AnamnesisReferral();
        referral.setAnamnesis(anamnesis);
        referral.setProfessional(sender);
        referral.setAssistant(receiver); // Pode ser null
        referral.setSelectedFieldsJson(selectedFieldsJson);

        return referralRepository.save(referral);
    }

    public AnamnesisReferral assignAssistant(Long referralId, Long assistantId) {
        if (assistantId == null) {
            throw new IllegalArgumentException("Assistant ID não pode ser nulo");
        }

        AnamnesisReferral referral = referralRepository.findById(referralId)
                .orElseThrow(() -> new RuntimeException("Encaminhamento não encontrado com ID: " + referralId));

        Assistant assistant = assistantRepository.findById(assistantId)
                .orElseThrow(() -> new RuntimeException("Assistente não encontrado com ID: " + assistantId));

        referral.setAssistant(assistant);
        return referralRepository.save(referral);
    }
}
