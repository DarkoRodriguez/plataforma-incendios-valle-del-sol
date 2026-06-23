package com.valledelsol.usuarios.auth;

import com.valledelsol.usuarios.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class JwtServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtService = new JwtService(jwtEncoder, "http://localhost:8081", 60);
    }

    @Test
    void testGenerateAccessToken() {
        User user = new User("user1", "password");
        user.setId(10L);
        user.setEmail("user1@example.com");
        user.setRole("ADMINISTRATOR");

        Jwt jwt = org.mockito.Mockito.mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("fake-token");
        when(jwtEncoder.encode(any())).thenReturn(jwt);

        String token = jwtService.generateAccessToken(user);

        assertEquals("fake-token", token);
    }

    @Test
    void testGenerateAccessTokenUsesUsernameWhenEmailBlank() {
        User user = new User("user1", "password");
        user.setId(10L);
        user.setEmail("");
        user.setRole("USER");

        Jwt jwt = org.mockito.Mockito.mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("fake-token-2");
        when(jwtEncoder.encode(any())).thenReturn(jwt);

        String token = jwtService.generateAccessToken(user);

        assertEquals("fake-token-2", token);
    }

    @Test
    void testGetAccessTokenTtlSeconds() {
        assertEquals(3600, jwtService.getAccessTokenTtlSeconds());
    }
}
