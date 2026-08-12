package com.app.gerencia.controllers.dto;

import com.app.gerencia.entities.Secretary;

public record SecretaryDTO(
        Long   id,
        String name,
        String cpf,
        String email,
        String phoneNumber
) {
    public static SecretaryDTO fromEntity(Secretary s) {
        return new SecretaryDTO(
                s.getId(),
                s.getName(),
                s.getCpf(),
                s.getEmail(),
                s.getPhoneNumber()
        );
    }
}

