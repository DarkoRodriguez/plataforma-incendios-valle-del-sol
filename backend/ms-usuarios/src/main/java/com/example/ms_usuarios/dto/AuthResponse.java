package com.example.ms_usuarios.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
