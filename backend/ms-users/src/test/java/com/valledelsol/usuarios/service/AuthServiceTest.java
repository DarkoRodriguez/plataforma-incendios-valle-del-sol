package com.valledelsol.usuarios.service;

import com.valledelsol.usuarios.auth.AuthStrategy;
import com.valledelsol.usuarios.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Mock
    private AuthStrategy passwordStrategy;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthService(java.util.List.of(passwordStrategy));
    }

    @Test
    void testAuthenticateUsesPasswordStrategy() {
        User user = new User();
        when(passwordStrategy.name()).thenReturn("password");
        when(passwordStrategy.authenticate("user", "pass")).thenReturn(Optional.of(user));

        Optional<User> result = authService.authenticate("user", "pass");

        assertTrue(result.isPresent());
        assertSame(user, result.get());
    }
}
