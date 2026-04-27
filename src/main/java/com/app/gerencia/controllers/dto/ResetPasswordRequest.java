package com.app.gerencia.controllers.dto;

public record ResetPasswordRequest(String token, String newPassword) {
}
