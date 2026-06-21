package com.example.ms_usuarios.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ms_usuarios.dto.AuthResponseDTO;
import com.example.ms_usuarios.dto.LoginRequest;
import com.example.ms_usuarios.dto.RegisterRequest;
import com.example.ms_usuarios.dto.UserDTO;
import com.example.ms_usuarios.model.User;
import com.example.ms_usuarios.service.UserService;
import com.example.ms_usuarios.auth.JwtService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/usuarios/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        if (userService.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        User user = new User();
        user.setUsername(request.getUsername().trim());
        if (request.getCorreo() != null) {
            user.setCorreo(request.getCorreo().trim().toLowerCase());
        }
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRegion(request.getRegion());
        user.setComuna(request.getComuna());
        user.setTelefono(request.getTelefono());
        user.setRol(request.getRol() != null ? request.getRol().toUpperCase() : "USUARIO");
        userService.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequest request) {
        String ident = request.getUsername().trim();
        return userService.findByUsername(ident)
                .or(() -> userService.findByCorreo(ident))
                .filter(u -> passwordEncoder.matches(request.getPassword(), u.getPassword()))
                .map(u -> {
                    String token = jwtService.generateAccessToken(u);
                    UserDTO dto = new UserDTO(
                            u.getId(), u.getUsername(), u.getRegion(), u.getComuna(), u.getCorreo(), u.getTelefono(), u.getRol()
                    );
                    return ResponseEntity.ok(new AuthResponseDTO(token, dto));
                })
                .orElse(ResponseEntity.status(401).build());
    }
}
