package com.app.gerencia.services;

import com.app.gerencia.controllers.dto.CreateContractRequest;
import com.app.gerencia.entities.*;
import com.app.gerencia.enums.ContractStatus;
import com.app.gerencia.enums.ParticipantRole;
import com.app.gerencia.repository.*;
import jakarta.transaction.Transactional;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ContractService {

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private ContractParticipantRepository participantRepository;

    @Autowired
    private ContractNotificationService contractNotificationService;

    @Autowired
    private GuardianRepository guardianRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private SecretaryRepository secretaryRepository;

//    @Transactional
//    public Contract createContract(
//            CreateContractRequest request,
//            String ip
//    ) {
//
//        Guardian guardian = guardianRepository.findById(
//                request.getGuardianId()
//        ).orElseThrow(() ->
//                new RuntimeException("Responsável não encontrado")
//        );
//
//        Patient patient = patientRepository.findById(
//                request.getPatientId()
//        ).orElseThrow(() ->
//                new RuntimeException("Paciente não encontrado")
//        );
//
//        List<Secretary> witnesses = request.getWitnesses()
//                .stream()
//                .map(w -> secretaryRepository.findById(w.getSecretaryId())
//                        .orElseThrow(() ->
//                                new RuntimeException("Secretário não encontrado")
//                        )
//                )
//                .toList();
//
//        Contract contract = new Contract();
//        contract.setGuardian(guardian);
//        contract.setPatient(patient);
//        contract.setCreatedIp(ip);
//        contract.setCreatedAt(LocalDateTime.now());
//        contract.setStatus(ContractStatus.PENDING_SIGNATURES);
//
//        contract = contractRepository.save(contract);
//
//        int order = 1;
//
//        createParticipant(contract, guardian, null,
//                ParticipantRole.CONTRACTOR, order++);
//
//        for (Secretary secretary : witnesses) {
//            createParticipant(contract, null, secretary,
//                    ParticipantRole.WITNESS, order++);
//        }
//
//        contractNotificationService.sendNextEmail(contract.getId());
//
//        return contract;
//    }

    public Contract createContract(
            CreateContractRequest request,
            MultipartFile file,
            String ip
    ) {

        Contract contract = new Contract();
        contract.setCreatedIp(ip);
        contract.setCreatedAt(LocalDateTime.now());

        var guardian = guardianRepository.findById(request.getGuardianId())
                .orElseThrow(() -> new RuntimeException("Responsável não encontrado"));

        var patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado"));

        contract.setGuardian(guardian);
        contract.setPatient(patient);

        // upload
        if (Boolean.TRUE.equals(request.getUploaded())) {

            if (file == null || file.isEmpty()) {
                throw new RuntimeException("Arquivo PDF é obrigatório");
            }

            try {
                contract.setPdfData(file.getBytes());
                contract.setPdfFileName(file.getOriginalFilename());
                contract.setPdfContentType(file.getContentType());
            } catch (Exception e) {
                throw new RuntimeException("Erro ao ler arquivo", e);
            }

            contract.setStatus(ContractStatus.COMPLETED);

            return contractRepository.save(contract);
        }

        // gerar contrato
        contract.setStatus(ContractStatus.PENDING_SIGNATURES);

        contract = contractRepository.save(contract);

        int order = 1;

        // CONTRATANTE
        ContractParticipant contractor = new ContractParticipant();
        contractor.setContract(contract);
        contractor.setGuardian(guardian);
        contractor.setRole(ParticipantRole.CONTRACTOR);
        contractor.setSigningOrder(order++);
        contractor.setSigned(false);
        contractor.setToken(UUID.randomUUID().toString());

        participantRepository.save(contractor);

        // TESTEMUNHAS (OPCIONAL)
        if (Boolean.TRUE.equals(request.getHasWitnesses())) {

            for (CreateContractRequest.WitnessRequest w : request.getWitnesses()) {

                var secretary = secretaryRepository.findById(w.getSecretaryId())
                        .orElseThrow(() -> new RuntimeException("Secretária não encontrada"));

                ContractParticipant witness = new ContractParticipant();
                witness.setContract(contract);
                witness.setUser(secretary);
                witness.setRole(ParticipantRole.WITNESS);
                witness.setSigningOrder(order++);
                witness.setSigned(false);
                witness.setToken(UUID.randomUUID().toString());

                participantRepository.save(witness);
            }
        }

        contractNotificationService.sendNextEmail(contract.getId());

        return contract;
    }

    private void createParticipant(
            Contract contract,
            Guardian guardian,
            User user,
            ParticipantRole role,
            int order
    ) {
        ContractParticipant p = new ContractParticipant();
        p.setContract(contract);
        p.setGuardian(guardian);
        p.setUser(user);
        p.setRole(role);
        p.setSigningOrder(order);
        p.setSigned(false);
        p.setToken(UUID.randomUUID().toString());

        participantRepository.save(p);
    }


}

