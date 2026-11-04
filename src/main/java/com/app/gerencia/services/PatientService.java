package com.app.gerencia.services;

import com.app.gerencia.entities.Anamnesis;
import com.app.gerencia.entities.Contract;
import com.app.gerencia.entities.Guardian;
import com.app.gerencia.entities.Patient;
import com.app.gerencia.enums.ContractStatus;
import com.app.gerencia.enums.PatientStatus;
import com.app.gerencia.repository.AnamnesisAnswerRepository;
import com.app.gerencia.repository.AnamnesisRepository;
import com.app.gerencia.repository.AppointmentRepository;
import com.app.gerencia.repository.ContractRepository;
import com.app.gerencia.repository.FrequencyRepository;
import com.app.gerencia.repository.ChargeRepository;
import com.app.gerencia.repository.GuardianRepository;
import com.app.gerencia.repository.PatientRepository;
import com.app.gerencia.repository.RecordRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private GuardianRepository guardianRepository;

    // Dependências usadas apenas para o cascade manual de exclusão do paciente (delete())
    @Autowired
    private FrequencyRepository frequencyRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private RecordRepository recordRepository;

    @Autowired
    private AnamnesisRepository anamnesisRepository;

    @Autowired
    private AnamnesisAnswerRepository anamnesisAnswerRepository;

    @Autowired
    private ContractRepository contractRepository;
    @Autowired
    private ChargeRepository chargeRepository;

    public String save(Patient patient) {
        if (patient.getGuardian() != null) {
            Guardian guardianData = patient.getGuardian();
            Guardian guardian;

            if (guardianData.getId() != null) {
                guardian = guardianRepository.findById(guardianData.getId())
                        .orElseThrow(() -> new RuntimeException("Responsável não encontrado"));
            } else {
                guardian = guardianRepository.save(guardianData);
            }

            patient.setGuardian(guardian);
        }

        patientRepository.save(patient);
        return "Salvo com sucesso!";
    }

    public List<Patient> findByCpf(String cpf){
        return patientRepository.findByCpfContainingIgnoreCase(cpf);
    }

    public Patient findById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado com id " + id));
    }


    public List<Patient> findByName(String nome) {
        return patientRepository.findByNameContainingIgnoreCase(nome);
    }

    public List<Patient> findAll(){

        return patientRepository.findAll();
    }

    // Calcula status e data de início do paciente a partir dos contratos dele (ignora
    // contratos CANCELADO) e preenche os campos transientes de Patient:
    //  - contractStartDate = data de início do contrato mais antigo (quando o paciente "entrou")
    //  - status = ATIVO se existir algum contrato cujo fim ainda não chegou; senão INATIVO
    public Patient enrichWithContractStatus(Patient patient) {
        List<Contract> contracts = contractRepository.findByPatientIdOrderByCreatedAtDesc(patient.getId());
        LocalDate hoje = LocalDate.now();

        LocalDate inicio = contracts.stream()
                .filter(c -> c.getStatus() != ContractStatus.CANCELADO)
                .map(Contract::getStartDate)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);

        boolean ativo = contracts.stream()
                .anyMatch(c -> c.getStatus() != ContractStatus.CANCELADO
                        && c.getEndDate() != null
                        && !c.getEndDate().isBefore(hoje));

        patient.setContractStartDate(inicio);
        patient.setStatus((ativo ? PatientStatus.ATIVO : PatientStatus.INATIVO).name());
        return patient;
    }

    public List<Patient> enrichAllWithContractStatus(List<Patient> patients) {
        patients.forEach(this::enrichWithContractStatus);
        return patients;
    }

    @Transactional
    public String update(Patient patient, Long id) {

        Patient existingPatient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado"));


        existingPatient.setName(patient.getName());
        existingPatient.setCpf(patient.getCpf());
        existingPatient.setDateBirth(patient.getDateBirth());
        existingPatient.setKinship(patient.getKinship());


        if (patient.getGuardian() != null) {
            existingPatient.setGuardian(patient.getGuardian());
        }


        if (patient.getPhoto() != null && patient.getPhoto().length > 0) {
            existingPatient.setPhoto(patient.getPhoto());
        }


        patientRepository.save(existingPatient);

        return "Paciente atualizado com sucesso!";
    }

    // Exclusão em cascata: remove manualmente tudo que referencia o paciente antes de excluí-lo,
    // já que nem toda relação declara cascade JPA (ex: Frequency/Appointment não cascateiam a
    // partir do Patient) e não há ON DELETE CASCADE no banco (ddl-auto=update não configura isso).
    // Ordem importa: filhos antes dos pais, para nunca violar uma FK no meio do caminho.
    @Transactional
    public String delete(Long id) {

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado"));

        // Bloqueio do financeiro: checado antes de qualquer exclusão em cascata.
        if (chargeRepository.existsByPatientId(id)) {
            throw new IllegalStateException("Paciente possui cobranças financeiras associadas e não pode ser excluído");
        }

        // Agenda: Frequency referencia o paciente diretamente e não tem cascade a partir de
        // Appointment (proposital, para nunca apagar histórico de presença sozinho) — remove
        // as frequências primeiro, depois os agendamentos.
        frequencyRepository.deleteAllByPatientId(id);
        appointmentRepository.deleteAllByPatientId(id);

        // Fichas de atendimento (só depois de remover agendamentos/frequências, que as referenciam)
        recordRepository.deleteAllByPatientId(id);

        // Anamneses: as respostas não têm cascade a partir de Anamnesis, então são removidas
        // explicitamente; o encaminhamento (referral) já cascateia via JPA ao remover a anamnese.
        List<Anamnesis> anamneses = anamnesisRepository.findByPatientId(id);
        for (Anamnesis anamnesis : anamneses) {
            anamnesisAnswerRepository.deleteAllByAnamnesisId(anamnesis.getId());
        }
        anamnesisRepository.deleteAll(anamneses);

        // Contratos: participantes, assinaturas e aceites já cascateiam via JPA a partir do Contract
        List<Contract> contracts = contractRepository.findByPatientIdOrderByCreatedAtDesc(id);
        contractRepository.deleteAll(contracts);

        patientRepository.delete(patient);

        return "Excluído com sucesso!";
    }

    public List<Patient> searchByGuardian(Long id){

        return patientRepository.findByGuardianId(id);
    }
    public List<Patient> searchByNameOrCpf(String query) {

        String cleanQuery = query.replaceAll("[^0-9]", "");


        if (cleanQuery.length() >= 3) {

            return patientRepository.findByNameContainingIgnoreCaseOrCpfContaining(query, query);
        } else {

            return patientRepository.findByNameContainingIgnoreCase(query);
        }
    }

    public List<Patient> findByBirthMonth(Integer month) {
        int targetMonth = (month != null) ? month : LocalDate.now().getMonthValue();
        return patientRepository.findByBirthMonth(targetMonth);
    }
}
