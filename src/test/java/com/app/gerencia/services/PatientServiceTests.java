package com.app.gerencia.services;

import com.app.gerencia.entities.Anamnesis;
import com.app.gerencia.entities.Contract;
import com.app.gerencia.entities.Patient;
import com.app.gerencia.repository.AnamnesisAnswerRepository;
import com.app.gerencia.repository.AnamnesisRepository;
import com.app.gerencia.repository.AppointmentRepository;
import com.app.gerencia.repository.ChargeRepository;
import com.app.gerencia.repository.ContractRepository;
import com.app.gerencia.repository.FrequencyRepository;
import com.app.gerencia.repository.GuardianRepository;
import com.app.gerencia.repository.PatientRepository;
import com.app.gerencia.repository.RecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Cobre PatientService.delete(): o bloqueio de exclusão do módulo financeiro (paciente com
 * cobrança não pode ser excluído) e a exclusão em cascata manual (agenda, fichas, anamneses,
 * contratos). Os demais métodos de PatientService não são cobertos aqui.
 */
@ExtendWith(MockitoExtension.class)
class PatientServiceTests {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private GuardianRepository guardianRepository;

    @Mock
    private ChargeRepository chargeRepository;

    @Mock
    private FrequencyRepository frequencyRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private RecordRepository recordRepository;

    @Mock
    private AnamnesisRepository anamnesisRepository;

    @Mock
    private AnamnesisAnswerRepository anamnesisAnswerRepository;

    @Mock
    private ContractRepository contractRepository;

    @InjectMocks
    private PatientService patientService;

    @Test
    void delete_patientWithAssociatedCharge_throwsAndDeletesNothing() {
        Patient patient = new Patient();
        patient.setId(1L);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(chargeRepository.existsByPatientId(1L)).thenReturn(true);

        assertThatThrownBy(() -> patientService.delete(1L))
                .isInstanceOf(IllegalStateException.class);

        verify(frequencyRepository, never()).deleteAllByPatientId(anyLong());
        verify(appointmentRepository, never()).deleteAllByPatientId(anyLong());
        verify(recordRepository, never()).deleteAllByPatientId(anyLong());
        verify(anamnesisRepository, never()).deleteAll(any());
        verify(contractRepository, never()).deleteAll(any());
        verify(patientRepository, never()).delete(any());
    }

    @Test
    void delete_patientWithoutCharge_cascadesChildrenThenDeletesPatientOnce() {
        Patient patient = new Patient();
        patient.setId(2L);
        Anamnesis anamnesis = new Anamnesis();
        anamnesis.setId(10L);
        List<Anamnesis> anamneses = List.of(anamnesis);
        List<Contract> contracts = List.of(new Contract());
        when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));
        when(chargeRepository.existsByPatientId(2L)).thenReturn(false);
        when(anamnesisRepository.findByPatientId(2L)).thenReturn(anamneses);
        when(contractRepository.findByPatientIdOrderByCreatedAtDesc(2L)).thenReturn(contracts);

        String result = patientService.delete(2L);

        assertThat(result).isEqualTo("Excluído com sucesso!");
        InOrder order = inOrder(frequencyRepository, appointmentRepository, recordRepository,
                anamnesisAnswerRepository, anamnesisRepository, contractRepository, patientRepository);
        order.verify(frequencyRepository).deleteAllByPatientId(2L);
        order.verify(appointmentRepository).deleteAllByPatientId(2L);
        order.verify(recordRepository).deleteAllByPatientId(2L);
        order.verify(anamnesisAnswerRepository).deleteAllByAnamnesisId(10L);
        order.verify(anamnesisRepository).deleteAll(anamneses);
        order.verify(contractRepository).deleteAll(contracts);
        order.verify(patientRepository).delete(patient);
        verify(patientRepository, times(1)).delete(patient);
    }
}
