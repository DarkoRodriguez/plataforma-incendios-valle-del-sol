package com.example.ms_mapeo.auth;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

/**
 * Utilidad de autenticación JWT para el microservicio de mapeo.
 * Valida los tokens RS256 consultando el JWKS publicado por el servicio de usuarios.
 */
@Component
public class JwtUtil {

    private final String jwksUrl;
    private volatile PublicKey publicKey;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtUtil(@Value("${security.jwt.jwks-url:http://ms-usuarios:8081/.well-known/jwks.json}") String jwksUrl) {
        this.jwksUrl = jwksUrl;
        try {
            this.publicKey = fetchPublicKeyFromJwks();
        } catch (Exception e) {
            this.publicKey = null;
        }
    }

    private PublicKey fetchPublicKeyFromJwks() throws Exception {
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(jwksUrl)).GET().build();
        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new IOException("Failed to fetch jwks: " + resp.statusCode());
        }
        JsonNode root = objectMapper.readTree(resp.body());
        JsonNode keys = root.get("keys");
        if (keys == null || !keys.isArray() || keys.size() == 0) {
            throw new IOException("No keys in jwks");
        }
        JsonNode jwk = keys.get(0);
        String kty = jwk.path("kty").asText();
        if (!"RSA".equalsIgnoreCase(kty)) {
            throw new IOException("Unsupported key type: " + kty);
        }
        String n = jwk.path("n").asText();
        String e = jwk.path("e").asText();
        byte[] nb = Base64.getUrlDecoder().decode(n);
        byte[] eb = Base64.getUrlDecoder().decode(e);
        BigInteger modulus = new BigInteger(1, nb);
        BigInteger exponent = new BigInteger(1, eb);
        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    private Claims parseTokenInternal(String token) throws Exception {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (publicKey == null) {
            synchronized (this) {
                if (publicKey == null) {
                    publicKey = fetchPublicKeyFromJwks();
                }
            }
        }
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Claims parseToken(String token) {
        try {
            return parseTokenInternal(token);
        } catch (Exception e) {
            try {
                this.publicKey = fetchPublicKeyFromJwks();
                return parseTokenInternal(token);
            } catch (Exception ex) {
                throw new RuntimeException("Invalid token", ex);
            }
        }
    }

    public Long getUserId(String token) {
        try {
            return parseToken(token).get("userId", Long.class);
        } catch (Exception e) {
            return null;
        }
    }

    public String getRole(String token) {
        try {
            return parseToken(token).get("role", String.class);
        } catch (Exception e) {
            return null;
        }
    }
}
