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

@Service
public class ReporteIncendioService {

    @Autowired
    private ReporteIncendioRepository repository;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public ReporteIncendioDTO crearReporte(ReporteIncendioDTO dto) {
        ReporteIncendio entity = new ReporteIncendio();
        entity.setDescripcion(dto.getDescripcion());
        entity.setTipo(dto.getTipo() != null ? dto.getTipo() : "FORESTAL");
        entity.setEstado(dto.getEstado() != null ? dto.getEstado() : "ACTIVO");

        Point point = geometryFactory.createPoint(new Coordinate(dto.getLongitud(), dto.getLatitud()));
        entity.setUbicacion(point);

        entity = repository.save(entity);
        return mapToDTO(entity);
    }

    public List<ReporteIncendioDTO> obtenerTodos() {
        return repository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

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
        return dto;
    }
}
