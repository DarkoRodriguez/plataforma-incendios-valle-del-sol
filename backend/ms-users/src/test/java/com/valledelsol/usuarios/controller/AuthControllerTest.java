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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private UserService userService;

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

        when(userService.registerUser(any(RegisterRequest.class))).thenReturn(new User());

        ResponseEntity<Void> response = authController.register(req);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(userService, times(1)).registerUser(any(RegisterRequest.class));
    }

    @Test
    void testRegisterUsernameConflict() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("newuser");
        req.setPassword("passwd");

        when(userService.registerUser(any(RegisterRequest.class)))
                .thenThrow(new IllegalArgumentException("Nombre de usuario ya existe"));

        ResponseEntity<Void> response = authController.register(req);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userService, times(1)).registerUser(any(RegisterRequest.class));
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

        when(userService.authenticate(req)).thenReturn(Optional.of(mockUser));
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

        when(userService.authenticate(req)).thenReturn(Optional.empty());

        ResponseEntity<AuthResponseDTO> response = authController.login(req);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
