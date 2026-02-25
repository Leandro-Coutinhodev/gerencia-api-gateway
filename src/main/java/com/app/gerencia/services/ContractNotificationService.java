package com.app.gerencia.services;

import com.app.gerencia.entities.Contract;
import com.app.gerencia.entities.ContractParticipant;
import com.app.gerencia.repository.ContractParticipantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Service
public class ContractNotificationService {

    private static final Logger log = LoggerFactory.getLogger(ContractNotificationService.class);

    @Autowired
    private ContractParticipantRepository participantRepository;

    @Autowired
    private EmailService emailService;

    @Value("${link.host}")
    private String frontendUrl;

    public void sendNextEmail(Long contractId) {

        List<ContractParticipant> participants =
                participantRepository
                        .findByContractIdOrderBySigningOrder(contractId);

        participants.stream()
                .filter(p -> !Boolean.TRUE.equals(p.getSigned()))
                .findFirst()
                .ifPresent(this::sendSignatureEmail);
    }

    /**
     * Envia o PDF do contrato assinado para o e-mail do contratante.
     */
    public void sendSignedContractToContractor(Contract contract) {

        if (contract.getPdfPath() == null) {
            log.warn("Tentativa de enviar contrato {} sem PDF gerado.", contract.getId());
            return;
        }

        File pdfFile = new File(contract.getPdfPath());
        if (!pdfFile.exists()) {
            log.error("PDF do contrato {} não encontrado em: {}", contract.getId(), contract.getPdfPath());
            return;
        }

        String email = contract.getGuardian().getEmail();
        String name = contract.getGuardian().getName();
        String patientName = contract.getPatient() != null
                ? contract.getPatient().getName()
                : "N/A";

        String subject = "Contrato assinado - LP Kids";

        String body = """
                Olá %s,

                Seu contrato referente ao(à) paciente %s foi assinado por todas as partes com sucesso.

                Segue em anexo o contrato completo com o registro das assinaturas eletrônicas.

                Identificador do contrato: %s

                Caso tenha dúvidas, entre em contato conosco.

                Atenciosamente,
                Equipe LP Kids
                """.formatted(name, patientName, contract.getHash());

        String fileName = String.format("contrato_%d_assinado.pdf", contract.getId());

        try {
            byte[] pdfBytes = Files.readAllBytes(pdfFile.toPath());

            emailService.sendEmailWithAttachment(
                    email,
                    subject,
                    body,
                    pdfBytes,
                    fileName
            );
        } catch (IOException e) {
            log.error("Erro ao ler PDF do contrato {} para envio: {}", contract.getId(), e.getMessage());
            return;
        }

        log.info("Contrato {} assinado enviado para {}", contract.getId(), email);
    }

    private void sendSignatureEmail(ContractParticipant participant) {

        String email;
        String name;

        if (participant.getGuardian() != null) {
            email = participant.getGuardian().getEmail();
            name = participant.getGuardian().getName();
        } else {
            email = participant.getUser().getEmail();
            name = participant.getUser().getName();
        }

        String link = frontendUrl + "/contrato/" + participant.getToken();

        String subject = "Assinatura de contrato pendente";

        String body = """
                Olá %s,

                Você possui um contrato pendente de assinatura.

                Para visualizar e assinar o contrato, clique no link abaixo:
                %s

                Caso não reconheça esta solicitação, ignore este e-mail.

                Atenciosamente,
                Equipe Gerência
                """.formatted(name, link);

        emailService.sendEmailWithAttachment(
                email,
                subject,
                body,
                null,
                null
        );
    }
}