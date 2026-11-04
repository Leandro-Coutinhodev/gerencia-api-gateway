package com.app.gerencia.enums;

// Status calculado do paciente a partir dos contratos dele — nunca persistido
// (ver Patient.status / PatientService.enrichWithContractStatus).
public enum PatientStatus {
    ATIVO, INATIVO
}
