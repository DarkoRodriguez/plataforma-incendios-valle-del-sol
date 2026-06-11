package com.example.ms_usuarios.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ms_usuarios.dto.UserDTO;
import com.example.ms_usuarios.dto.AuthResponseDTO;
import com.example.ms_usuarios.auth.JwtUtil;
import com.example.ms_usuarios.model.User;
import com.example.ms_usuarios.service.AuthService;
import com.example.ms_usuarios.service.UserService;

/**
 * Controlador REST encargado de gestionar el ciclo de vida de los usuarios:
 * registro, autenticación, obtención de perfiles y actualización segura.
 */
@RestController
@RequestMapping("/api/usuarios")
public class UserController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Endpoint para registrar un nuevo usuario en la plataforma.
     * 
     * @param req Datos de registro que incluyen credenciales y datos de localización/contacto.
     * @return Respuesta con el Token JWT generado y los datos públicos del usuario registrado.
     */
    @PostMapping("/auth/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody RegisterRequest req) {
        if (userService.findByUsername(req.username).isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        User newUser = new User(req.username, req.password);
        newUser.setRegion(req.region);
        newUser.setComuna(req.comuna);
        newUser.setCorreo(req.correo);
        newUser.setTelefono(req.telefono);
        if (req.rol != null) {
            newUser.setRol(req.rol.toUpperCase());
        }
        User savedUser = userService.save(newUser);
        String token = jwtUtil.generateToken(savedUser);
        return ResponseEntity.ok(new AuthResponseDTO(token, new UserDTO(
            savedUser.getId(), savedUser.getUsername(), savedUser.getRegion(),
            savedUser.getComuna(), savedUser.getCorreo(), savedUser.getTelefono(), savedUser.getRol()
        )));
    }

    /**
     * Endpoint para autenticar a un usuario mediante sus credenciales.
     * 
     * @param req Nombre de usuario y contraseña.
     * @return Respuesta con el Token JWT correspondiente y los detalles del perfil si el login es exitoso.
     */
    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequest req) {
        return authService.authenticate(req.username, req.password)
                .map(u -> {
                    String token = jwtUtil.generateToken(u);
                    return ResponseEntity.ok(new AuthResponseDTO(token, new UserDTO(
                        u.getId(), u.getUsername(), u.getRegion(),
                        u.getComuna(), u.getCorreo(), u.getTelefono(), u.getRol()
                    )));
                })
                .orElse(ResponseEntity.status(401).build());
    }

    /**
     * Obtiene los datos públicos de un usuario por su identificador único.
     * 
     * @param id Identificador único del usuario.
     * @return Detalles públicos mapeados a un DTO, o 404 Not Found si no existe.
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        return userService.findById(id)
                .map(u -> ResponseEntity.ok(new UserDTO(
                    u.getId(), u.getUsername(), u.getRegion(),
                    u.getComuna(), u.getCorreo(), u.getTelefono(), u.getRol()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Actualiza los datos de un usuario de forma segura con verificación de JWT.
     * Bloquea la autoelevación de roles a usuarios normales e impide que terceros
     * editen perfiles ajenos (a menos que sean ADMINISTRADORES).
     * 
     * @param id Identificador del usuario que se desea actualizar.
     * @param req Datos actualizados opcionales.
     * @param authHeader Cabecera de autorización Bearer Token.
     * @return Datos públicos del usuario actualizados, o código de error correspondiente.
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id, 
            @RequestBody UpdateRequest req,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Falta cabecera de Autorización");
        }

        try {
            Long tokenUserId = jwtUtil.getUserId(authHeader);
            String tokenRole = jwtUtil.getRole(authHeader);

            if (tokenUserId == null || (!tokenUserId.equals(id) && !tokenRole.equalsIgnoreCase("ADMINISTRADOR"))) {
                return ResponseEntity.status(403).body("No tienes permisos para modificar este perfil");
            }

            return userService.findById(id).map(u -> {
                u.setUsername(req.username);
                u.setRegion(req.region);
                u.setComuna(req.comuna);
                u.setCorreo(req.correo);
                u.setTelefono(req.telefono);
                
                if (req.password != null && !req.password.trim().isEmpty()) {
                    u.setPassword(req.password);
                }
                
                // ¡Solo administradores pueden alterar roles para evitar escalabilidad!
                if (req.rol != null && tokenRole.equalsIgnoreCase("ADMINISTRADOR")) {
                    u.setRol(req.rol.toUpperCase());
                }

                userService.save(u);
                return ResponseEntity.ok(new UserDTO(
                    u.getId(), u.getUsername(), u.getRegion(),
                    u.getComuna(), u.getCorreo(), u.getTelefono(), u.getRol()
                ));
            }).orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Token inválido o expirado");
        }
    }

    /**
     * Payload de entrada para el registro de nuevos usuarios.
     */
    static class RegisterRequest {
        public String username;
        public String password;
        public String region;
        public String comuna;
        public String correo;
        public String telefono;
        public String rol;
    }

    /**
     * Payload de entrada para el inicio de sesión.
     */
    static class LoginRequest {
        public String username;
        public String password;
    }

    /**
     * Payload de entrada para la actualización de perfiles de usuario.
     */
    static class UpdateRequest {
        public String username;
        public String password;
        public String region;
        public String comuna;
        public String correo;
        public String telefono;
        public String rol;
    }
}
