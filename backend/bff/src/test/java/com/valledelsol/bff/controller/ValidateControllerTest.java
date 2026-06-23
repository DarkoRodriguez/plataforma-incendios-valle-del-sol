package com.valledelsol.bff.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidateControllerTest {

    private final ValidateController controller = new ValidateController();

    @Test
    void testValidateSuccess() {
        Jwt jwt = new Jwt(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "none"),
                Map.of("sub", "12345", "role", "ADMINISTRATOR", "iss", "https://issuer.example")
        );

        Map<String, Object> result = controller.validate(jwt);

        assertTrue((Boolean) result.get("valid"));
        assertEquals("12345", result.get("subject"));
        assertEquals("ADMINISTRATOR", ((Map<?, ?>) result.get("claims")).get("role"));
        assertEquals("https://issuer.example", result.get("issuer"));
    }

    @Test
    void testFallbackUsesRequestUriWhenHeaderMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/alerts/fallback");

        Map<String, Object> result = controller.fallback(request);

        assertEquals("SERVICE_UNAVAILABLE", result.get("status"));
        assertEquals("El backend de destino no está disponible. Intente nuevamente más tarde.", result.get("message"));
        assertEquals("/api/alerts/fallback", result.get("path"));
    }

    @Test
    void testValidateHandlesNullIssuer() {
        Jwt jwt = new Jwt(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "none"),
                Map.of("sub", "12345", "role", "ADMINISTRATOR")
        );

        Map<String, Object> result = controller.validate(jwt);

        assertTrue((Boolean) result.get("valid"));
        assertEquals("12345", result.get("subject"));
        assertEquals("ADMINISTRATOR", ((Map<?, ?>) result.get("claims")).get("role"));
        assertEquals(null, result.get("issuer"));
    }
}
