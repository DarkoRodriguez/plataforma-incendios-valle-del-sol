package com.valledelsol.usuarios.controller;

import com.valledelsol.usuarios.auth.JwtService;
import com.valledelsol.usuarios.dto.AuthResponseDTO;
import com.valledelsol.usuarios.dto.LoginRequest;
import com.valledelsol.usuarios.dto.RegisterRequest;
import com.valledelsol.usuarios.model.User;
import com.valledelsol.usuarios.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterSuccess() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("newuser");
        req.setPassword("passwd");
        req.setEmail("newuser@mail.com");
        req.setRole("USER");

        when(userService.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("passwd")).thenReturn("encoded-pwd");

        ResponseEntity<Void> response = authController.register(req);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(userService, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterUsernameConflict() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("newuser");
        req.setPassword("passwd");

        User existingUser = new User();
        when(userService.findByUsername("newuser")).thenReturn(Optional.of(existingUser));

        ResponseEntity<Void> response = authController.register(req);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userService, never()).save(any(User.class));
    }

    @Test
    void testLoginSuccess() {
        LoginRequest req = new LoginRequest();
        req.setUsername("loginuser");
        req.setPassword("password123");

        User mockUser = new User("loginuser", "encoded-pwd");
        mockUser.setId(100L);
        mockUser.setEmail("loginuser@mail.com");
        mockUser.setRole("USER");

        when(userService.findByUsername("loginuser")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password123", "encoded-pwd")).thenReturn(true);
        when(jwtService.generateAccessToken(mockUser)).thenReturn("fake-jwt-token");

        ResponseEntity<AuthResponseDTO> response = authController.login(req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("fake-jwt-token", response.getBody().getToken());
        assertEquals("loginuser", response.getBody().getUser().getUsername());
    }

    @Test
    void testLoginUnauthorized() {
        LoginRequest req = new LoginRequest();
        req.setUsername("loginuser");
        req.setPassword("wrongpassword");

        User mockUser = new User("loginuser", "encoded-pwd");

        when(userService.findByUsername("loginuser")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrongpassword", "encoded-pwd")).thenReturn(false);

        ResponseEntity<AuthResponseDTO> response = authController.login(req);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
