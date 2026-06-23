package com.valledelsol.usuarios.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.valledelsol.usuarios.dto.AuthResponseDTO;
import com.valledelsol.usuarios.dto.LoginRequest;
import com.valledelsol.usuarios.dto.RegisterRequest;
import com.valledelsol.usuarios.dto.UserDTO;
import com.valledelsol.usuarios.service.UserService;
import com.valledelsol.usuarios.auth.JwtService;

import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/users/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        try {
            userService.registerUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequest request) {
        return userService.authenticate(request)
                .map(u -> {
                    String token = jwtService.generateAccessToken(u);
                    UserDTO dto = new UserDTO(
                            u.getId(), u.getUsername(), u.getRegion(), u.getCommune(), u.getEmail(), u.getPhone(), u.getRole()
                    );
                    return ResponseEntity.ok(new AuthResponseDTO(token, dto));
                })
                .orElse(ResponseEntity.status(401).build());
    }
}
