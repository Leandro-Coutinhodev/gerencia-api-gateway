package com.app.gerencia.repository;

import com.app.gerencia.entities.Frequency;
import com.app.gerencia.enums.FrequencyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FrequencyRepository extends JpaRepository<Frequency, Long> {

    // Consulta única usada tanto pela agenda/calendário quanto pela listagem de frequências e
    // pelo resumo do dashboard. professionalId nulo = todos os profissionais (uso restrito ao admin).
    @Query("""
            SELECT f FROM Frequency f
            WHERE (:professionalId IS NULL OR f.professional.id = :professionalId)
              AND (:patientId IS NULL OR f.patient.id = :patientId)
              AND (:status IS NULL OR f.status = :status)
              AND f.scheduledDate BETWEEN :startDate AND :endDate
            ORDER BY f.scheduledDate ASC, f.scheduledTime ASC
            """)
    List<Frequency> search(@Param("professionalId") Long professionalId,
                            @Param("patientId") Long patientId,
                            @Param("status") FrequencyStatus status,
                            @Param("startDate") LocalDate startDate,
                            @Param("endDate") LocalDate endDate);

    // Usada na checagem de conflito de horário (RB10)
    List<Frequency> findByProfessionalIdAndScheduledDateAndStatusNot(
            Long professionalId, LocalDate scheduledDate, FrequencyStatus excludedStatus);

    // Cancelamento em lote (escopos FROM_HERE/ALL) — atinge apenas ocorrências ainda pendentes
    // (AGENDADO), nunca sobrescrevendo ocorrências já resolvidas (PRESENTE/AUSENTE/JUSTIFICADO)
    List<Frequency> findByAppointmentIdAndStatus(Long appointmentId, FrequencyStatus status);

    List<Frequency> findByAppointmentIdAndScheduledDateGreaterThanEqualAndStatus(
            Long appointmentId, LocalDate fromDate, FrequencyStatus status);

    boolean existsByAppointmentIdAndStatus(Long appointmentId, FrequencyStatus status);
}
