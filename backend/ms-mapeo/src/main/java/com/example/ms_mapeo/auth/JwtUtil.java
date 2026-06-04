package com.example.ms_mapeo.auth;

import java.security.Key;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

/**
 * Utilidad de autenticación JWT para el microservicio de mapeo.
 * Permite decodificar, validar firmas criptográficas y extraer roles y identificadores
 * de usuario de forma local con la clave estática simétrica compartida.
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
     * Decodifica y extrae los claims del token JWT validando su firma criptográfica.
     * 
     * @param token Token JWT recibido (con o sin prefijo "Bearer ").
     * @return Claims decodificados del token.
     */
    public Claims parseToken(String token) {
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
}
