package com.valledelsol.usuarios.controller;

import com.valledelsol.usuarios.dto.UserDTO;
import com.valledelsol.usuarios.model.User;
import com.valledelsol.usuarios.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

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
        UserDTO dto = new UserDTO(2L, "testuser", null, null, null, null, "USER");

        when(userService.getAllUsersForAdmin("Bearer valid-token")).thenReturn(List.of(dto));

        ResponseEntity<?> response = userController.getAllUsers("Bearer valid-token");

        assertEquals(200, response.getStatusCode().value());
        List<?> body = (List<?>) response.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
    }

    @Test
    void testGetAllUsersUnauthorized() {
        when(userService.getAllUsersForAdmin(null))
                .thenThrow(new IllegalArgumentException("Falta cabecera de Autorización"));

        ResponseEntity<?> response = userController.getAllUsers(null);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetAllUsersForbidden() {
        when(userService.getAllUsersForAdmin("Bearer normal-token"))
                .thenThrow(new SecurityException("Acceso denegado: se requieren permisos de Administrador"));

        ResponseEntity<?> response = userController.getAllUsers("Bearer normal-token");
        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void testUpdateUserSuccess() {
        UserDTO updatedDto = new UserDTO(5L, "new-name", null, null, "new@mail.com", null, "USER");

        when(userService.updateUser(
                eq(5L),
                eq("Bearer user-token"),
                eq("new-name"),
                isNull(),
                isNull(),
                isNull(),
                eq("new@mail.com"),
                isNull(),
                isNull()
        )).thenReturn(updatedDto);

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
        UserDTO updatedDto = new UserDTO(10L, "someuser", null, null, null, null, "BRIGADIST");

        when(userService.updateUserRole(10L, "BRIGADIST", "Bearer admin-token")).thenReturn(updatedDto);

        UserController.RoleUpdateRequest req = new UserController.RoleUpdateRequest();
        req.role = "BRIGADIST";

        ResponseEntity<?> response = userController.updateUserRole(10L, req, "Bearer admin-token");
        assertEquals(200, response.getStatusCode().value());

        UserDTO body = (UserDTO) response.getBody();
        assertNotNull(body);
        assertEquals("BRIGADIST", body.getRole());
    }
}
