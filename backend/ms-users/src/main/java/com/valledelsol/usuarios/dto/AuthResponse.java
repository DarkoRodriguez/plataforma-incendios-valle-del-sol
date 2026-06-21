package com.valledelsol.usuarios.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
