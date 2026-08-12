package com.app.gerencia.services;

import com.app.gerencia.entities.*;
import com.app.gerencia.repository.ContractParticipantRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ContractNotificationService {

    private final ContractParticipantRepository participantRepository;
    private final EmailService emailService;

    @Value("${link.host}")
    private String frontendUrl;

    public ContractNotificationService(
            ContractParticipantRepository participantRepository,
            EmailService emailService
    ) {
        this.participantRepository = participantRepository;
        this.emailService = emailService;
    }


    public void sendNextSequential(Long contractId) {
        participantRepository.findPendingOrderedByContractId(contractId)
                .stream().findFirst()
                .ifPresent(this::sendSignatureEmail);
    }


    public void sendAllParallel(Long contractId) {
        participantRepository.findPendingOrderedByContractId(contractId)
                .forEach(this::sendSignatureEmail);
    }

    public void sendFinalPdfToGuardian(Contract contract, byte[] pdfBytes) {
        Guardian guardian = contract.getGuardian();
        if (guardian == null || guardian.getEmail() == null) return;

        String subject = "Contrato assinado – LP Kids";
        String body = """
            Olá %s,
 
            Seu contrato referente ao(à) paciente %s foi assinado por todas as partes.
 
            Segue em anexo o contrato completo com o registro das assinaturas eletrônicas.
 
            Identificador: %s
 
            Atenciosamente,
            Equipe LP Kids
            """.formatted(
                guardian.getName(),
                contract.getPatient() != null ? contract.getPatient().getName() : "N/A",
                contract.getHash()
        );

        emailService.sendEmailWithAttachment(
                guardian.getEmail(), subject, body,
                pdfBytes, "contrato_" + contract.getId() + "_assinado.pdf"
        );
    }

    private void sendSignatureEmail(ContractParticipant participant) {
        String link = frontendUrl + "/contrato/" + participant.getToken();
        String name  = participant.getName();
        String email = participant.getEmail();
        if (email == null) return;

        String subject = "Assinatura de contrato pendente – LP Kids";
        String body = """
            Olá %s,
 
            Você possui um contrato pendente de assinatura.
 
            Clique no link abaixo para visualizar e assinar:
            %s
 
            Caso não reconheça esta solicitação, ignore este e-mail.
 
            Atenciosamente,
            Equipe LP Kids
            """.formatted(name, link);

        emailService.sendEmailWithAttachment(email, subject, body, null, null);
    }
}
