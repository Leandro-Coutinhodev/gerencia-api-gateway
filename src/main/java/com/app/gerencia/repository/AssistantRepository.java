package com.app.gerencia.repository;

import com.app.gerencia.entities.Assistant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssistantRepository extends JpaRepository<Assistant,Long> {
}
