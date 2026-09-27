package com.app.gerencia.repository;

import com.app.gerencia.entities.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByProfessionalIdOrderByNameAsc(Long professionalId);
    List<Activity> findByProfessionalIdAndActiveTrueOrderByNameAsc(Long professionalId);
    List<Activity> findAllByOrderByNameAsc();
}
