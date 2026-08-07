package com.app.gerencia.controllers.dto.contract;

import com.app.gerencia.entities.Contract;
import com.app.gerencia.entities.ContractParticipant;
import com.app.gerencia.enums.ContractStatus;
import com.app.gerencia.enums.ParticipantRole;

import java.time.LocalDateTime;
import java.util.List;

public record ContractDTO(
        Long id,
        Long templateId,
        String templateName,
        Long patientId,
        String patientName,
        Long guardianId,
        String guardianName,
        ContractStatus status,
        Boolean hasWitnesses,
        LocalDateTime createdAt,
        String hash,
        List<ParticipantDTO> participants
) {
    public static ContractDTO fromEntity(Contract c) {
        return new ContractDTO(
                c.getId(),
                c.getTemplate() != null ? c.getTemplate().getId() : null,
                c.getTemplate() != null ? c.getTemplate().getName() : null,
                c.getPatient() != null ? c.getPatient().getId() : null,
                c.getPatient() != null ? c.getPatient().getName() : null,
                c.getGuardian() != null ? c.getGuardian().getId() : null,
                c.getGuardian() != null ? c.getGuardian().getName() : null,
                c.getStatus(),
                c.getHasWitnesses(),
                c.getCreatedAt(),
                c.getHash(),
                c.getParticipants() == null ? List.of() :
                        c.getParticipants().stream().map(ParticipantDTO::fromEntity).toList()
        );
    }

    public record ParticipantDTO(
            Long id, ParticipantRole role, String name, String email, String cpf,
            Integer signingOrder, ContractParticipant.SigningStatus signingStatus,
            LocalDateTime signedAt
    ) {
        public static ParticipantDTO fromEntity(ContractParticipant p) {
            LocalDateTime signedAt = p.getSignature() != null
                    ? p.getSignature().getSignedAt() : null;
            return new ParticipantDTO(p.getId(), p.getRole(), p.getName(), p.getEmail(),
                    p.getCpf(), p.getSigningOrder(), p.getSigningStatus(), signedAt);
        }
    }
}
