package com.example.ms_usuarios.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ms_usuarios.dto.UserDTO;
import com.example.ms_usuarios.service.AuthService;
import com.example.ms_usuarios.service.UserService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @PostMapping("/auth/login")
    public ResponseEntity<UserDTO> login(@RequestBody LoginRequest req) {
        return authService.authenticate(req.username, req.password)
                .map(u -> ResponseEntity.ok(new UserDTO(u.getId(), u.getUsername())))
                .orElse(ResponseEntity.status(401).build());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        return userService.findById(id)
                .map(u -> ResponseEntity.ok(new UserDTO(u.getId(), u.getUsername())))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UpdateRequest req) {
        return userService.findById(id).map(u -> {
            u.setUsername(req.username);
            userService.save(u);
            return ResponseEntity.ok(new UserDTO(u.getId(), u.getUsername()));
        }).orElse(ResponseEntity.notFound().build());
    }

    static class LoginRequest {
        public String username;
        public String password;
    }

    static class UpdateRequest {
        public String username;
    }
}
