package com.app.gerencia.entities;

import com.app.gerencia.enums.RecurrenceFrequencyType;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Regra de recorrência de um agendamento (Appointment). Uma única instância representa toda
// a série — as ocorrências concretas (uma por data gerada a partir desta regra) são as
// Frequency ligadas ao Appointment dono desta recorrência.
@Entity
@Table(name = "tb_appointment_recurrence")
public class AppointmentRecurrence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_recurrence_id")
    private Long id;

    @Column(name = "frequency_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private RecurrenceFrequencyType frequencyType;

    // Dias da semana (ISO: segunda=1 ... domingo=7), separados por "|". Usado apenas em SEMANAL.
    @Column(name = "days_of_week")
    private String daysOfWeek;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public RecurrenceFrequencyType getFrequencyType() { return frequencyType; }
    public void setFrequencyType(RecurrenceFrequencyType frequencyType) { this.frequencyType = frequencyType; }

    public String getDaysOfWeek() { return daysOfWeek; }
    public void setDaysOfWeek(String daysOfWeek) { this.daysOfWeek = daysOfWeek; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
