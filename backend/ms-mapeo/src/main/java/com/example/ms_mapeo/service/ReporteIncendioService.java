package com.example.ms_mapeo.service;

import com.example.ms_mapeo.dto.ReporteIncendioDTO;
import com.example.ms_mapeo.model.ReporteIncendio;
import com.example.ms_mapeo.repository.ReporteIncendioRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio encargado de gestionar las operaciones lógicas de negocio para reportes de incendio:
 * creación, obtención de focos activos/históricos y modificación de estados.
 */
@Service
public class ReporteIncendioService {

    @Autowired
    private ReporteIncendioRepository repository;

    /**
     * Factoría de geometría espacial de JTS configurada con el sistema de coordenadas de referencia SRID 4326 (WGS84).
     */
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    /**
     * Crea un nuevo reporte de incendio, transformando coordenadas de latitud/longitud en un punto PostGIS.
     * 
     * @param dto Objeto con los datos del reporte.
     * @return El reporte creado mapeado a DTO.
     */
    public ReporteIncendioDTO crearReporte(ReporteIncendioDTO dto) {
        ReporteIncendio entity = new ReporteIncendio();
        entity.setDescripcion(dto.getDescripcion());
        entity.setTipo(dto.getTipo() != null ? dto.getTipo() : "FORESTAL");
        entity.setEstado(dto.getEstado() != null ? dto.getEstado() : "ACTIVO");

        Point point = geometryFactory.createPoint(new Coordinate(dto.getLongitud(), dto.getLatitud()));
        entity.setUbicacion(point);
        entity.setUsuarioId(dto.getUsuarioId());

        entity = repository.save(entity);
        return mapToDTO(entity);
    }

    /**
     * Obtiene todos los reportes de incendio de la base de datos mapeados a DTOs.
     * 
     * @return Lista de reportes.
     */
    public List<ReporteIncendioDTO> obtenerTodos() {
        return repository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    /**
     * Actualiza el estado operativo (ej: CONTROLADO, EXTINGUIDO) de un reporte específico.
     * 
     * @param id Identificador del reporte.
     * @param estado Nuevo estado a asignar.
     * @return DTO del reporte actualizado.
     */
    public ReporteIncendioDTO actualizarEstado(Long id, String estado) {
        ReporteIncendio entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reporte no encontrado"));
        entity.setEstado(estado);
        entity = repository.save(entity);
        return mapToDTO(entity);
    }

    /**
     * Mapea de forma interna una entidad JPA a un Objeto de Transferencia de Datos (DTO).
     * 
     * @param entity Entidad de base de datos.
     * @return DTO con coordenadas en punto plano y atributos legibles.
     */
    private ReporteIncendioDTO mapToDTO(ReporteIncendio entity) {
        ReporteIncendioDTO dto = new ReporteIncendioDTO();
        dto.setId(entity.getId());
        dto.setDescripcion(entity.getDescripcion());
        dto.setTipo(entity.getTipo());
        dto.setEstado(entity.getEstado());
        dto.setLatitud(entity.getUbicacion().getY());
        dto.setLongitud(entity.getUbicacion().getX());
        if (entity.getFechaReporte() != null) {
            dto.setFechaReporte(entity.getFechaReporte().toString());
        }
        dto.setUsuarioId(entity.getUsuarioId());
        return dto;
    }
}
