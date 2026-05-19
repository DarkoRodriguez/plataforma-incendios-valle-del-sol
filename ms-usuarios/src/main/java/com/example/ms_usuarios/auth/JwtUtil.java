package com.example.ms_usuarios.auth;

import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Component;

import com.example.ms_usuarios.model.User;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * Utilidad encargada de la generación, parseo y validación de tokens JWT en el microservicio de usuarios.
 * Utiliza una clave simétrica compartida estática para garantizar consistencia entre microservicios.
 */
@Component
public class JwtUtil {

    /**
     * Clave estática para firma de tokens JWT en el ecosistema (estilo simétrico HMAC).
     */
    private static final String SECRET_STRING = "valle_del_sol_super_secret_jwt_key_1234567890_valle_del_sol_super_secret_jwt_key";
    
    /**
     * Objeto Key generado con algoritmo HMAC-SHA a partir de la constante de clave.
     */
    private static final Key key = Keys.hmacShaKeyFor(SECRET_STRING.getBytes());
    
    /**
     * Tiempo de expiración por defecto de los tokens (24 horas en milisegundos).
     */
    private static final long EXPIRATION_TIME = 86400000;

    /**
     * Genera un Token JWT firmado digitalmente que incluye las credenciales e información clave del usuario.
     * 
     * @param user Entidad del usuario logueado.
     * @return Token JWT en formato String.
     */
    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userId", user.getId())
                .claim("role", user.getRol())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }

    /**
     * Decodifica y extrae los claims del token JWT validando su firma criptográfica.
     * 
     * @param token Token JWT recibido (con o sin prefijo "Bearer ").
     * @return Claims decodificados del token.
     */
    public io.jsonwebtoken.Claims parseToken(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extrae de forma segura el ID de usuario del token.
     * 
     * @param token Token JWT.
     * @return Identificador único del usuario, o null en caso de error.
     */
    public Long getUserId(String token) {
        try {
            return parseToken(token).get("userId", Long.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extrae de forma segura el Rol del usuario del token.
     * 
     * @param token Token JWT.
     * @return Nombre del Rol (USUARIO, BRIGADISTA, ADMINISTRADOR), o null en caso de error.
     */
    public String getRole(String token) {
        try {
            return parseToken(token).get("role", String.class);
        } catch (Exception e) {
            return null;
        }
    }
}
