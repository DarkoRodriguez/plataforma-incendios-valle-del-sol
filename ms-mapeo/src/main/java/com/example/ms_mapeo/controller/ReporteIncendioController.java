package com.example.ms_mapeo.controller;

import com.example.ms_mapeo.dto.ReporteIncendioDTO;
import com.example.ms_mapeo.service.ReporteIncendioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST encargado de exponer y gestionar los focos de incendio reportados.
 * Permite la visualización de incendios activos, el envío de reportes iniciales
 * y la actualización de estados por personal calificado (RBAC: Brigadistas y Administradores).
 */
@RestController
@RequestMapping("/api/mapeo/reportes")
public class ReporteIncendioController {

    @Autowired
    private ReporteIncendioService service;

    @Autowired
    private com.example.ms_mapeo.auth.JwtUtil jwtUtil;

    /**
     * Endpoint para reportar un nuevo foco de incendio en el mapa geográfico.
     * 
     * @param dto Contiene la descripción, tipo de incendio y coordenadas geográficas.
     * @return DTO del reporte creado con su ID autogenerado.
     */
    @PostMapping
    public ResponseEntity<ReporteIncendioDTO> crearReporte(@RequestBody ReporteIncendioDTO dto) {
        return ResponseEntity.ok(service.crearReporte(dto));
    }

    /**
     * Endpoint para obtener la lista de todos los focos de incendio registrados.
     * 
     * @return Lista completa de reportes geográficos.
     */
    @GetMapping
    public ResponseEntity<List<ReporteIncendioDTO>> obtenerTodos() {
        return ResponseEntity.ok(service.obtenerTodos());
    }

    /**
     * Endpoint seguro para actualizar el estado operativo de un incendio (ACTIVO, CONTROLADO, EXTINGUIDO).
     * Requiere autenticación JWT y valida que el usuario posea rol de BRIGADISTA o ADMINISTRADOR.
     * 
     * @param id Identificador único del reporte.
     * @param req Cuerpo con el nuevo estado.
     * @param authHeader Cabecera de Autorización conteniendo el Bearer Token.
     * @return Reporte de incendio modificado, o códigos HTTP de restricción/error de autenticación.
     */
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstado(
            @PathVariable Long id,
            @RequestBody EstadoRequest req,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Falta cabecera de Autorización");
        }

        try {
            String role = jwtUtil.getRole(authHeader);
            if (role == null || (!role.equalsIgnoreCase("BRIGADISTA") && !role.equalsIgnoreCase("ADMINISTRADOR"))) {
                return ResponseEntity.status(403).body("No tienes permisos de Brigadista para cambiar el estado");
            }
            
            ReporteIncendioDTO updated = service.actualizarEstado(id, req.estado);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Token inválido o expirado");
        }
    }

    /**
     * Estructura de petición interna para solicitar modificaciones de estado operativo.
     */
    static class EstadoRequest {
        public String estado;
    }
}
