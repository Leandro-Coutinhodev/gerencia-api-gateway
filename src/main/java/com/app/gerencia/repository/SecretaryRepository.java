package com.app.gerencia.repository;

import com.app.gerencia.entities.Secretary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecretaryRepository extends JpaRepository<Secretary, Long> {

    @Query("SELECT s FROM Secretary s WHERE " +
            "LOWER(s.name) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
            "LOWER(s.email) LIKE LOWER(CONCAT('%', :termo, '%'))")
    List<Secretary> findByNameOrEmailContaining(String termo);

}
