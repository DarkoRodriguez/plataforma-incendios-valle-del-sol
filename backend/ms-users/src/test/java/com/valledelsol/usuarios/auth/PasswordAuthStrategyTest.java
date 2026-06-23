package com.valledelsol.usuarios.auth;

import com.valledelsol.usuarios.model.User;
import com.valledelsol.usuarios.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class PasswordAuthStrategyTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordAuthStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testName() {
        assertEquals("password", strategy.name());
    }

    @Test
    void testAuthenticateSuccess() {
        User user = new User("user1", "encoded");
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "encoded")).thenReturn(true);

        Optional<User> result = strategy.authenticate("user1", "secret");

        assertTrue(result.isPresent());
        assertEquals("user1", result.get().getUsername());
    }
}
