package com.example.ms_usuarios.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad que representa a un usuario en la plataforma de control de incendios Valle del Sol.
 * Contiene información de autenticación, localización, contacto y rol de acceso.
 */
@Entity
@Table(name = "users")
public class User {

    /**
     * Identificador único autogenerado del usuario en la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre de usuario único para inicio de sesión.
     */
    @Column(unique = true, nullable = false)
    private String username;

    /**
     * Contraseña del usuario (en producción debe estar cifrada).
     */
    @Column(nullable = false)
    private String password;

    /**
     * Región geográfica de residencia o control del usuario.
     */
    @Column(nullable = true)
    private String region;

    /**
     * Comuna de residencia o control del usuario.
     */
    @Column(nullable = true)
    private String comuna;

    /**
     * Dirección de correo electrónico de contacto.
     */
    @Column(nullable = true)
    private String correo;

    /**
     * Número de teléfono o móvil de contacto rápido.
     */
    @Column(nullable = true)
    private String telefono;

    /**
     * Rol asignado en la plataforma (USUARIO, BRIGADISTA o ADMINISTRADOR).
     */
    @Column(nullable = false)
    private String rol = "USUARIO";

    /**
     * Constructor vacío requerido por JPA.
     */
    public User() {}

    /**
     * Constructor con los parámetros mínimos de autenticación.
     * 
     * @param username Nombre del usuario.
     * @param password Contraseña elegida.
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getComuna() {
        return comuna;
    }

    public void setComuna(String comuna) {
        this.comuna = comuna;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}
