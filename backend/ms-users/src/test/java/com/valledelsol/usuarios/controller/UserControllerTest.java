package com.valledelsol.usuarios.controller;

import com.valledelsol.usuarios.dto.UserDTO;
import com.valledelsol.usuarios.auth.JwtUtil;
import com.valledelsol.usuarios.model.User;
import com.valledelsol.usuarios.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

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

        ResponseEntity<UserDTO> response = userController.getUser(1L);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("admin", response.getBody().getUsername());
    }

    @Test
    void testGetUserNotFound() {
        when(userService.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<UserDTO> response = userController.getUser(1L);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testGetAllUsersSuccess() {
        User mockUser = new User("testuser", "pwd");
        mockUser.setId(2L);
        mockUser.setRole("USER");

        when(jwtUtil.getRole("Bearer valid-token")).thenReturn("ADMINISTRATOR");
        when(userService.findAll()).thenReturn(Collections.singletonList(mockUser));

        ResponseEntity<?> response = userController.getAllUsers("Bearer valid-token");
        assertEquals(200, response.getStatusCode().value());
        
        List<?> body = (List<?>) response.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
    }

    @Test
    void testGetAllUsersUnauthorized() {
        ResponseEntity<?> response = userController.getAllUsers(null);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetAllUsersForbidden() {
        when(jwtUtil.getRole("Bearer normal-token")).thenReturn("USER");

        ResponseEntity<?> response = userController.getAllUsers("Bearer normal-token");
        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void testUpdateUserSuccess() {
        User mockUser = new User("username", "pwd");
        mockUser.setId(5L);
        mockUser.setRole("USER");

        when(jwtUtil.getUserId("Bearer user-token")).thenReturn(5L);
        when(jwtUtil.getRole("Bearer user-token")).thenReturn("USER");
        when(userService.findById(5L)).thenReturn(Optional.of(mockUser));
        when(userService.save(any(User.class))).thenReturn(mockUser);

        UserController.UpdateRequest req = new UserController.UpdateRequest();
        req.username = "new-name";
        req.email = "new@mail.com";

        ResponseEntity<?> response = userController.updateUser(5L, req, "Bearer user-token");
        assertEquals(200, response.getStatusCode().value());

        UserDTO body = (UserDTO) response.getBody();
        assertNotNull(body);
        assertEquals("new-name", body.getUsername());
    }

    @Test
    void testUpdateUserRoleSuccess() {
        User mockUser = new User("someuser", "pwd");
        mockUser.setId(10L);
        mockUser.setRole("USER");

        when(jwtUtil.getRole("Bearer admin-token")).thenReturn("ADMINISTRATOR");
        when(userService.findById(10L)).thenReturn(Optional.of(mockUser));
        when(userService.save(any(User.class))).thenReturn(mockUser);

        UserController.RoleUpdateRequest req = new UserController.RoleUpdateRequest();
        req.role = "BRIGADIST";

        ResponseEntity<?> response = userController.updateUserRole(10L, req, "Bearer admin-token");
        assertEquals(200, response.getStatusCode().value());

        UserDTO body = (UserDTO) response.getBody();
        assertNotNull(body);
        assertEquals("BRIGADIST", body.getRole());
    }
}
