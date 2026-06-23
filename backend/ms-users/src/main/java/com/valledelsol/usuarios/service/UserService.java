package com.valledelsol.usuarios.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.valledelsol.usuarios.auth.JwtUtil;
import com.valledelsol.usuarios.dto.LoginRequest;
import com.valledelsol.usuarios.dto.RegisterRequest;
import com.valledelsol.usuarios.dto.UserDTO;
import com.valledelsol.usuarios.model.User;
import com.valledelsol.usuarios.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public User registerUser(RegisterRequest request) {
        if (findByUsername(request.getUsername().trim()).isPresent()) {
            throw new IllegalArgumentException("Nombre de usuario ya existe");
        }
        if (request.getEmail() != null && findByEmail(request.getEmail().trim().toLowerCase()).isPresent()) {
            throw new IllegalArgumentException("Correo ya registrado");
        }

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail() != null ? request.getEmail().trim().toLowerCase() : null);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRegion(request.getRegion());
        user.setCommune(request.getCommune());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole() != null ? request.getRole().toUpperCase() : "USER");

        return save(user);
    }

    public Optional<User> authenticate(LoginRequest request) {
        String ident = request.getUsername().trim();
        return findByUsername(ident)
                .or(() -> findByEmail(ident))
                .filter(u -> passwordEncoder.matches(request.getPassword(), u.getPassword()));
    }

    public List<UserDTO> getAllUsersForAdmin(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Falta cabecera de Autorización");
        }

        String role = jwtUtil.getRole(authHeader);
        if (role == null) {
            throw new IllegalArgumentException("Token inválido o expirado");
        }

        if (!role.equalsIgnoreCase("ADMINISTRATOR")) {
            throw new SecurityException("Acceso denegado: se requieren permisos de Administrador");
        }

        return findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public UserDTO updateUser(Long id, String authHeader, String username, String password, String region, String commune, String email, String phone, String role) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Falta cabecera de Autorización");
        }

        Long tokenUserId = jwtUtil.getUserId(authHeader);
        String tokenRole = jwtUtil.getRole(authHeader);
        if (tokenUserId == null) {
            throw new IllegalArgumentException("Token inválido o expirado");
        }

        if (!tokenUserId.equals(id) && !"ADMINISTRATOR".equalsIgnoreCase(tokenRole)) {
            throw new SecurityException("No tienes permisos para modificar este perfil");
        }

        User user = findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (username != null) {
            user.setUsername(username);
        }
        if (region != null) {
            user.setRegion(region);
        }
        if (commune != null) {
            user.setCommune(commune);
        }
        if (email != null) {
            user.setEmail(email);
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        if (password != null && !password.trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        }
        if (role != null && "ADMINISTRATOR".equalsIgnoreCase(tokenRole)) {
            user.setRole(role.toUpperCase());
        }

        return mapToDTO(save(user));
    }

    public UserDTO updateUserRole(Long id, String role, String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Falta cabecera de Autorización");
        }

        String tokenRole = jwtUtil.getRole(authHeader);
        if (!"ADMINISTRATOR".equalsIgnoreCase(tokenRole)) {
            throw new SecurityException("Acceso denegado: se requieren permisos de Administrador");
        }

        User user = findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setRole(role.toUpperCase());
        return mapToDTO(save(user));
    }

    private UserDTO mapToDTO(User u) {
        return new UserDTO(u.getId(), u.getUsername(), u.getRegion(), u.getCommune(), u.getEmail(), u.getPhone(), u.getRole());
    }
}
