package com.app.gerencia.controllers.dto;

public record LoginResponse(String accessToken, Long expiresIn) {
}
