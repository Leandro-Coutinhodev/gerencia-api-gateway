package com.app.gerencia.controllers.dto;

import java.util.List;

public class CreateContractRequest {

    private Long guardianId;
    private Long patientId;

    private Boolean uploaded;
    private Boolean hasWitnesses;

    private List<WitnessRequest> witnesses;

    public Long getGuardianId() {
        return guardianId;
    }

    public void setGuardianId(Long guardianId) {
        this.guardianId = guardianId;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Boolean getUploaded() {
        return uploaded;
    }

    public void setUploaded(Boolean uploaded) {
        this.uploaded = uploaded;
    }

    public Boolean getHasWitnesses() {
        return hasWitnesses;
    }

    public void setHasWitnesses(Boolean hasWitnesses) {
        this.hasWitnesses = hasWitnesses;
    }

    public List<WitnessRequest> getWitnesses() {
        return witnesses;
    }

    public void setWitnesses(List<WitnessRequest> witnesses) {
        this.witnesses = witnesses;
    }

    public static class WitnessRequest {
        private Long secretaryId;

        public Long getSecretaryId() {
            return secretaryId;
        }

        public void setSecretaryId(Long secretaryId) {
            this.secretaryId = secretaryId;
        }
    }
}