package com.valledelsol.usuarios.dto;

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
    private String commune;
    
    /**
     * Correo electrónico de contacto.
     */
    private String email;
    
    /**
     * Número de teléfono de contacto.
     */
    private String phone;
    
    /**
     * Rol asignado en la plataforma.
     */
    private String role;

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
     * @param commune Comuna geográfica.
     * @param email Dirección de correo.
     * @param phone Número de teléfono.
     * @param role Rol en el sistema.
     */
    public UserDTO(Long id, String username, String region, String commune, String email, String phone, String role) {
        this.id = id;
        this.username = username;
        this.region = region;
        this.commune = commune;
        this.email = email;
        this.phone = phone;
        this.role = role;
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

    public String getCommune() {
        return commune;
    }

    public void setCommune(String commune) {
        this.commune = commune;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
