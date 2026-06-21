package com.valledelsol.bff.controller;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ValidateControllerTest {

    private final ValidateController controller = new ValidateController();

    @Test
    void testValidateSuccess() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("12345");
        when(jwt.getExpiresAt()).thenReturn(Instant.now().plusSeconds(3600));
        when(jwt.getClaims()).thenReturn(Map.of("role", "ADMINISTRATOR"));

        Map<String, Object> result = controller.validate(jwt);

        assertTrue((Boolean) result.get("valid"));
        assertEquals("12345", result.get("subject"));
        assertEquals(Map.of("role", "ADMINISTRATOR"), result.get("claims"));
    }
}
