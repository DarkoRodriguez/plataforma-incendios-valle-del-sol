package com.valledelsol.bff.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ValidateController {

    @GetMapping("/validate")
    public Map<String, Object> validate(@AuthenticationPrincipal Jwt jwt) {
        return Map.of(
                "valid", true,
                "subject", jwt.getSubject(),
                "issuer", jwt.getIssuer() != null ? jwt.getIssuer().toString() : null,
                "expiresAt", jwt.getExpiresAt(),
                "claims", jwt.getClaims()
        );
    }

    @GetMapping("/fallback")
    public Map<String, Object> fallback(HttpServletRequest request) {
        return Map.of(
                "status", "SERVICE_UNAVAILABLE",
                "message", "El backend de destino no está disponible. Intente nuevamente más tarde.",
                "path", request.getHeader("X-Original-URI") != null ? request.getHeader("X-Original-URI") : request.getRequestURI()
        );
    }
}
