package com.valledelsol.usuarios.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.valledelsol.usuarios.dto.UserDTO;
import com.valledelsol.usuarios.model.User;
import com.valledelsol.usuarios.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST encargado de gestionar los perfiles de usuario y roles de forma segura.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Obtiene la lista completa de todos los usuarios registrados.
     * Restringido a administradores.
     */
    @GetMapping
    public ResponseEntity<?> getAllUsers(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            return ResponseEntity.ok(userService.getAllUsersForAdmin(authHeader));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Token inválido o expirado");
        }
    }

    /**
     * Obtiene los datos públicos de un usuario por su identificador único.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        return userService.findById(id)
                .map(u -> ResponseEntity.ok(new UserDTO(
                        u.getId(), u.getUsername(), u.getRegion(),
                        u.getCommune(), u.getEmail(), u.getPhone(), u.getRole()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Actualiza los datos de un usuario de forma segura con verificación de JWT.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody UpdateRequest req,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        try {
            UserDTO updated = userService.updateUser(
                    id,
                    authHeader,
                    req.username,
                    req.password,
                    req.region,
                    req.commune,
                    req.email,
                    req.phone,
                    req.role
            );
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Token inválido o expirado");
        }
    }

    /**
     * Endpoint administrativo específico para modificar el rol de un usuario.
     */
    @PutMapping("/{id}/role")
    public ResponseEntity<?> updateUserRole(
            @PathVariable Long id,
            @RequestBody RoleUpdateRequest req,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        try {
            UserDTO updated = userService.updateUserRole(id, req.role, authHeader);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Token inválido o expirado");
        }
    }

    static class UpdateRequest {
        public String username;
        public String password;
        public String region;
        public String commune;
        public String email;
        public String phone;
        public String role;
    }

    static class RoleUpdateRequest {
        public String role;
    }
}
