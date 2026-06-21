package com.example.ms_usuarios.auth;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import com.example.ms_usuarios.model.User;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final long accessTokenMinutes;

    public JwtService(
            JwtEncoder jwtEncoder,
            @Value("${security.jwt.issuer:http://localhost:8081}") String issuer,
            @Value("${security.jwt.access-token-minutes:60}") long accessTokenMinutes
    ) {
        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.accessTokenMinutes = accessTokenMinutes;
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiration = now.plus(accessTokenMinutes, ChronoUnit.MINUTES);

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .id(UUID.randomUUID().toString())
            .issuer(issuer)
            .issuedAt(now)
            .expiresAt(expiration)
            .subject(user.getCorreo() != null && !user.getCorreo().isBlank() ? user.getCorreo() : user.getUsername())
            .claim("name", user.getUsername())
            .claim("role", user.getRol())
            .claim("userId", user.getId())
            .audience(java.util.List.of("bff"))
            .build();

        JwsHeader jwsHeader = JwsHeader.with(SignatureAlgorithm.RS256)
                .keyId("users-key-1")
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    public long getAccessTokenTtlSeconds() {
        return accessTokenMinutes * 60;
    }
}
