package com.app.gerencia.controllers.dto.record;

import com.app.gerencia.entities.Activity;

import java.time.LocalDateTime;

public record ActivityDTO(
        Long id,
        Long professionalId,
        String professionalName,
        String name,
        String description,
        String category,
        boolean active,
        LocalDateTime createdAt
) {
    public static ActivityDTO fromEntity(Activity a) {
        return new ActivityDTO(
                a.getId(),
                a.getProfessional() != null ? a.getProfessional().getId() : null,
                a.getProfessional() != null ? a.getProfessional().getName() : null,
                a.getName(),
                a.getDescription(),
                a.getCategory(),
                a.isActive(),
                a.getCreatedAt()
        );
    }
}
