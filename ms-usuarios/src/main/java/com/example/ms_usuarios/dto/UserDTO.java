package com.example.ms_usuarios.dto;

/**
 * Objeto de Transferencia de Datos (DTO) para representar la información pública de un usuario.
 * Excluye la contraseña por motivos de seguridad en las respuestas de la API.
 */
public class UserDTO {
    
    /**
     * Identificador único del usuario.
     */
    private Long id;
    
    /**
     * Nombre de usuario.
     */
    private String username;
    
    /**
     * Región de residencia o cobertura.
     */
    private String region;
    
    /**
     * Comuna de residencia o cobertura.
     */
    private String comuna;
    
    /**
     * Correo electrónico de contacto.
     */
    private String correo;
    
    /**
     * Número de teléfono de contacto.
     */
    private String telefono;
    
    /**
     * Rol asignado en la plataforma.
     */
    private String rol;

    /**
     * Constructor vacío requerido para deserialización de JSON.
     */
    public UserDTO() {}

    /**
     * Constructor completo para instanciar el DTO a partir de la entidad.
     * 
     * @param id Identificador único.
     * @param username Nombre de usuario.
     * @param region Región geográfica.
     * @param comuna Comuna geográfica.
     * @param correo Dirección de correo.
     * @param telefono Número de teléfono.
     * @param rol Rol en el sistema.
     */
    public UserDTO(Long id, String username, String region, String comuna, String correo, String telefono, String rol) {
        this.id = id;
        this.username = username;
        this.region = region;
        this.comuna = comuna;
        this.correo = correo;
        this.telefono = telefono;
        this.rol = rol;
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
