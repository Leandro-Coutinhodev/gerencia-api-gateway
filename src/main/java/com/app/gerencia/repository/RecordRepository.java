package com.app.gerencia.repository;

import com.app.gerencia.entities.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecordRepository extends JpaRepository<AttendanceRecord, Long> {
    List<AttendanceRecord> findByPatientIdOrderBySessionDateDesc(Long patientId);
    List<AttendanceRecord> findAllByOrderByCreatedAtDesc();
}
