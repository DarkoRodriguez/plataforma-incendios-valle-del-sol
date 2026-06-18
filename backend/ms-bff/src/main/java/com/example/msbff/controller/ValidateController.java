package com.example.msbff.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
}
