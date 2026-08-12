package com.app.gerencia.services;

import com.app.gerencia.controllers.dto.contract.*;
import com.app.gerencia.entities.*;
import com.app.gerencia.enums.ContractStatus;
import com.app.gerencia.enums.ParticipantRole;
import com.app.gerencia.repository.*;
import com.google.gson.Gson;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ContractService {

    private final ContractRepository contractRepository;
    private final ContractParticipantRepository participantRepository;
    private final ContractSignatureRepository signatureRepository;
    private final ContractTemplateRepository templateRepository;
    private final GuardianRepository guardianRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final ContractNotificationService notificationService;
    private final ContractPdfService pdfService;

    // Variáveis preenchidas automaticamente pelo sistema
    private static final Set<String> AUTO_VARIABLES = Set.of(
            "responsavel_nome", "responsavel_cpf", "responsavel_endereco",
            "paciente_nome", "paciente_cpf", "data_contrato"
    );

    public ContractService(
            ContractRepository contractRepository,
            ContractParticipantRepository participantRepository,
            ContractSignatureRepository signatureRepository,
            ContractTemplateRepository templateRepository,
            GuardianRepository guardianRepository,
            PatientRepository patientRepository,
            UserRepository userRepository,
            ContractNotificationService notificationService,
            ContractPdfService pdfService
    ) {
        this.contractRepository = contractRepository;
        this.participantRepository = participantRepository;
        this.signatureRepository = signatureRepository;
        this.templateRepository = templateRepository;
        this.guardianRepository = guardianRepository;
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.pdfService = pdfService;
    }



    @Transactional
    public Contract createForSigning(
            CreateContractForSigningRequestDTO req,
            String ip,
            Long createdByUserId
    ) {
        Patient patient = patientRepository.findById(req.patientId())
                .orElseThrow(() -> new EntityNotFoundException("Paciente não encontrado"));
        Guardian guardian = guardianRepository.findById(req.guardianId())
                .orElseThrow(() -> new EntityNotFoundException("Responsável não encontrado"));
        ContractTemplate template = templateRepository.findById(req.templateId())
                .orElseThrow(() -> new EntityNotFoundException("Modelo não encontrado"));

        Map<String, String> variableMap = buildVariableMap(req, patient, guardian);
        String renderedContent = renderContent(template, variableMap);
        boolean hasWitnesses = resolveHasWitnesses(template, req.hasWitnesses());

        Contract contract = new Contract();
        contract.setTemplate(template);
        contract.setPatient(patient);
        contract.setGuardian(guardian);
        contract.setStatus(ContractStatus.AGUARDANDO_ASSINATURA);
        contract.setVariablesData(new Gson().toJson(variableMap));
        contract.setRenderedContent(renderedContent);
        contract.setHasWitnesses(hasWitnesses);
        contract.setCreatedIp(ip);
        contract.setCreatedByUserId(createdByUserId);
        contract.setHash(generateHash(renderedContent));
        contract = contractRepository.save(contract);


        ContractParticipant responsavel = buildParticipant(
                contract, ParticipantRole.RESPONSAVEL,
                guardian.getName(), guardian.getEmail(), guardian.getCpf(),
                1, guardian, null
        );
        participantRepository.save(responsavel);


        ContractParticipant empresa = buildParticipant(
                contract, ParticipantRole.EMPRESA,
                "LP Kids", "contato@lpkids.com.br", "46.210.211/0001-60",
                2, null, null
        );

        empresa.setSigningStatus(ContractParticipant.SigningStatus.ASSINADO);
        participantRepository.save(empresa);


        ContractSignature empresaSignature = new ContractSignature();
        empresaSignature.setParticipant(empresa);
        empresaSignature.setSignedAt(LocalDateTime.now());
        empresaSignature.setSignedIp(ip); // IP de quem criou
        empresaSignature.setDocumentHash(contract.getHash());
        empresaSignature.setAcceptedTerms(true);
        signatureRepository.save(empresaSignature);


        int order = 3;
        if (hasWitnesses && req.witnessUserIds() != null) {
            for (Long userId : req.witnessUserIds()) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Usuário não encontrado: " + userId));
                ContractParticipant witness = buildParticipant(
                        contract, ParticipantRole.TESTEMUNHA,
                        user.getName(), user.getEmail(), user.getCpf(),
                        order++, null, user
                );
                participantRepository.save(witness);
            }
        }


        if (template.getSigningMode() == ContractTemplate.SigningMode.PARALELO) {
            notificationService.sendAllParallel(contract.getId());
        } else {
            notificationService.sendNextSequential(contract.getId());
        }

        return contract;
    }



    @Transactional
    public Contract createExternal(
            CreateExternalContractRequestDTO req,
            MultipartFile file,
            String ip,
            Long createdByUserId
    ) throws Exception {
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("Arquivo PDF é obrigatório");

        Patient patient = patientRepository.findById(req.patientId())
                .orElseThrow(() -> new EntityNotFoundException("Paciente não encontrado"));
        Guardian guardian = guardianRepository.findById(req.guardianId())
                .orElseThrow(() -> new EntityNotFoundException("Responsável não encontrado"));

        Contract contract = new Contract();
        contract.setPatient(patient);
        contract.setGuardian(guardian);
        contract.setStatus(ContractStatus.ASSINADO_EXTERNAMENTE);
        contract.setExternalPdfData(file.getBytes());
        contract.setExternalPdfFileName(file.getOriginalFilename());
        contract.setHash(generateHash(new String(file.getBytes(), StandardCharsets.UTF_8)));
        contract.setCreatedIp(ip);
        contract.setCreatedByUserId(createdByUserId);

        return contractRepository.save(contract);
    }


    @Transactional
    public void sign(String token, SignContractRequestDTO req, String ip) {
        if (!Boolean.TRUE.equals(req.acceptedTerms()))
            throw new IllegalArgumentException("É necessário aceitar os termos para assinar.");

        ContractParticipant participant = participantRepository.findByToken(token)
                .orElseThrow(() -> new EntityNotFoundException("Link de assinatura inválido"));

        if (participant.isSigned())
            throw new IllegalStateException("Você já assinou este contrato.");

        Contract contract = participant.getContract();
        ContractTemplate template = contract.getTemplate();

        // Valida ordem sequencial
        if (template != null && template.getSigningMode() == ContractTemplate.SigningMode.SEQUENCIAL) {
            ContractParticipant next = participantRepository
                    .findPendingOrderedByContractId(contract.getId())
                    .stream().findFirst()
                    .orElseThrow(() -> new IllegalStateException("Nenhum participante pendente"));

            if (!next.getId().equals(participant.getId()))
                throw new IllegalStateException("Ainda não é sua vez de assinar.");
        }


        if (req.acceptResponses() != null && !req.acceptResponses().isEmpty()) {


            if (participant.getAcceptResponses() == null) {
                participant.setAcceptResponses(new ArrayList<>());
            }

            participant.getAcceptResponses().clear();   // ← limpa sem trocar a referência

            for (Map.Entry<Long, String> entry : req.acceptResponses().entrySet()) {
                ContractAcceptField field = new ContractAcceptField();
                field.setId(entry.getKey());

                ContractAcceptResponse resp = new ContractAcceptResponse();
                resp.setParticipant(participant);
                resp.setAcceptField(field);
                resp.setResponseValue(entry.getValue());

                participant.getAcceptResponses().add(resp);   // ← adiciona na lista gerenciada
            }
        }

       // Assinatura
        ContractSignature signature = new ContractSignature();
        signature.setParticipant(participant);
        signature.setSignedAt(LocalDateTime.now());
        signature.setSignedIp(ip);
        signature.setDocumentHash(contract.getHash());
        signature.setAcceptedTerms(true);
        signatureRepository.save(signature);

        participant.setSignature(signature);

        participant.setSigningStatus(ContractParticipant.SigningStatus.ASSINADO);
        participantRepository.save(participant);


        boolean allSigned = participantRepository
                .findByContractIdOrderBySigningOrderAsc(contract.getId())
                .stream().allMatch(ContractParticipant::isSigned);

        if (allSigned) {
            contract.setStatus(ContractStatus.ASSINADO);
            contract.setUpdatedAt(LocalDateTime.now());

            try {
                byte[] pdf = pdfService.generateFinalPdf(contract);
                contract.setPdfData(pdf);
                contract.setPdfFileName("contrato_" + contract.getId() + "_assinado.pdf");
                notificationService.sendFinalPdfToGuardian(contract, pdf);
            } catch (Exception e) {
                e.printStackTrace();
            }

            contractRepository.save(contract);
        } else {
            contract.setStatus(ContractStatus.ASSINADO_PARCIALMENTE);
            contract.setUpdatedAt(LocalDateTime.now());
            contractRepository.save(contract);

            if (template != null
                    && template.getSigningMode() == ContractTemplate.SigningMode.SEQUENCIAL) {
                notificationService.sendNextSequential(contract.getId());
            }
        }
    }



    public ContractSigningViewDTO getSigningView(String token) {
        ContractParticipant participant = participantRepository.findByToken(token)
                .orElseThrow(() -> new EntityNotFoundException("Link de assinatura inválido"));

        Contract contract = participant.getContract();
        List<ContractTemplateDTO.AcceptFieldDTO> acceptFields = contract.getTemplate() != null
                && contract.getTemplate().getAcceptFields() != null
                ? contract.getTemplate().getAcceptFields().stream()
                .map(ContractTemplateDTO.AcceptFieldDTO::fromEntity).toList()
                : List.of();

        return new ContractSigningViewDTO(
                contract.getId(),
                participant.getName(),
                participant.getRole(),
                participant.getSigningStatus(),
                contract.getRenderedContent(),
                acceptFields
        );
    }



    public List<ContractDTO> findAll() {
        return contractRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(ContractDTO::fromEntity).toList();
    }

    public List<ContractDTO> findByPatient(Long patientId) {
        return contractRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream().map(ContractDTO::fromEntity).toList();
    }

    public byte[] getPdf(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new EntityNotFoundException("Contrato não encontrado"));

        if (contract.getPdfData() != null) return contract.getPdfData();

        if (contract.getExternalPdfData() != null) return contract.getExternalPdfData();
        throw new IllegalStateException("PDF indisponível para este contrato.");
    }

   // Funções auxiliares

    private Map<String, String> buildVariableMap(
            CreateContractForSigningRequestDTO req, Patient patient, Guardian guardian) {
        Map<String, String> map = new HashMap<>();


        map.put("responsavel_nome", guardian.getName() != null ? guardian.getName() : "");
        map.put("responsavel_cpf", guardian.getCpf() != null ? guardian.getCpf() : "");
        map.put("responsavel_endereco", guardian.getAddressLine1() != null ? guardian.getAddressLine1() : "");
        map.put("paciente_nome", patient.getName() != null ? patient.getName() : "");
        map.put("paciente_cpf", patient.getCpf() != null ? patient.getCpf() : "");
        map.put("data_contrato",
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy").format(LocalDateTime.now()));


        if (req.variableValues() != null) {
            map.putAll(req.variableValues());
        }

        return map;
    }


    private String renderContent(ContractTemplate template, Map<String, String> variables) {
        StringBuilder sb = new StringBuilder();

        if (template.getClauses() != null) {
            for (ContractClause clause : template.getClauses()) {
                sb.append("<section class=\"contract-clause\">");
                sb.append("<h3>").append(clause.getClauseOrder())
                        .append(". ").append(clause.getTitle()).append("</h3>");
                String content = clause.getContent();
                for (Map.Entry<String, String> entry : variables.entrySet()) {
                    content = content.replace("{{" + entry.getKey() + "}}", entry.getValue());
                }
                sb.append("<p>").append(content).append("</p>");
                sb.append("</section>");
            }
        }

        return sb.toString();
    }

    private boolean resolveHasWitnesses(ContractTemplate template, Boolean requestHasWitnesses) {
        return switch (template.getWitnessConfig()) {
            case OBRIGATORIO -> true;
            case NAO_UTILIZA -> false;
            case OPCIONAL    -> Boolean.TRUE.equals(requestHasWitnesses);
        };
    }

    private ContractParticipant buildParticipant(
            Contract contract, ParticipantRole role,
            String name, String email, String cpf,
            int order, Guardian guardian, User user
    ) {
        ContractParticipant p = new ContractParticipant();
        p.setContract(contract);
        p.setRole(role);
        p.setName(name);
        p.setEmail(email);
        p.setCpf(cpf);
        p.setSigningOrder(order);
        p.setToken(UUID.randomUUID().toString());
        p.setGuardian(guardian);
        p.setUser(user);
        return p;
    }

    private String generateHash(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(
                    (content + UUID.randomUUID()).getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            return UUID.randomUUID().toString();
        }
    }
}

