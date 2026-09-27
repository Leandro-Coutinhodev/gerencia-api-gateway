package com.app.gerencia.repository;

import com.app.gerencia.entities.Appointment;
import com.app.gerencia.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // professionalId nulo = todos os profissionais (uso restrito ao admin; nunca deve ser nulo
    // quando o requisitante é um profissional comum — resolvido no service)
    @Query("""
            SELECT a FROM Appointment a
            WHERE (:professionalId IS NULL OR a.professional.id = :professionalId)
              AND (:patientId IS NULL OR a.patient.id = :patientId)
              AND (:status IS NULL OR a.status = :status)
            ORDER BY a.createdAt DESC
            """)
    List<Appointment> search(@Param("professionalId") Long professionalId,
                              @Param("patientId") Long patientId,
                              @Param("status") AppointmentStatus status);
}
