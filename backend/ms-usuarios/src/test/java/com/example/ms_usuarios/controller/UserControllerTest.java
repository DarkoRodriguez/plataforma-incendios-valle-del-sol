package com.example.ms_usuarios.controller;

import com.example.ms_usuarios.model.User;
import com.example.ms_usuarios.service.AuthService;
import com.example.ms_usuarios.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class UserControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUserSuccess() {
        User mockUser = new User("admin", "password");
        mockUser.setId(1L);

        when(userService.findById(1L)).thenReturn(Optional.of(mockUser));

        ResponseEntity<?> response = userController.getUser(1L);
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testGetUserNotFound() {
        when(userService.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = userController.getUser(1L);
        assertEquals(404, response.getStatusCode());
    }
}
