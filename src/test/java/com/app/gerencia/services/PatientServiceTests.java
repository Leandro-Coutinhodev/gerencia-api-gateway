package com.app.gerencia.services;

import com.app.gerencia.entities.Patient;
import com.app.gerencia.repository.ChargeRepository;
import com.app.gerencia.repository.GuardianRepository;
import com.app.gerencia.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Cobre apenas a checagem de bloqueio de exclusão adicionada pelo módulo financeiro
 * (PatientService.delete() — ver design-backend.md). Os demais métodos de PatientService
 * não fazem parte do escopo do financeiro e não são cobertos aqui.
 */
@ExtendWith(MockitoExtension.class)
class PatientServiceTests {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private GuardianRepository guardianRepository;

    @Mock
    private ChargeRepository chargeRepository;

    @InjectMocks
    private PatientService patientService;

    @Test
    void delete_patientWithAssociatedCharge_throwsIllegalStateException() {
        Patient patient = new Patient();
        patient.setId(1L);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(chargeRepository.existsByPatientId(1L)).thenReturn(true);

        assertThatThrownBy(() -> patientService.delete(1L))
                .isInstanceOf(IllegalStateException.class);

        verify(patientRepository, never()).delete(any());
    }

    @Test
    void delete_patientWithoutCharge_deletesNormally() {
        Patient patient = new Patient();
        patient.setId(2L);
        when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));
        when(chargeRepository.existsByPatientId(2L)).thenReturn(false);

        String result = patientService.delete(2L);

        assertThat(result).isEqualTo("Excluído com sucesso!");
        verify(patientRepository).delete(patient);
    }
}
