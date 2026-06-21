package com.valledelsol.reports.service;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.valledelsol.reports.auth.JwtUtil;

import com.valledelsol.reports.dto.FireReportDTO;
import com.valledelsol.reports.model.FireReport;
import com.valledelsol.reports.repository.FireReportRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 
 * 
 * 
 * Servicio encargado de gestionar las operaciones de negocio de los reportes de
 * incendio.
 */
@Service
public class FireReportService {

    @Autowired
    private FireReportRepository repository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${alerts.service.url:http://ms-alerts:8083/api/alerts}")
    private String alertsServiceUrl;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    /**
     * Crea un nuevo reporte de incendio.
     */
    public FireReportDTO createReport(FireReportDTO dto) {
        FireReport entity = new FireReport();
        entity.setDescription(dto.getDescription());
        entity.setType(dto.getType() != null ? dto.getType() : "FORESTAL");
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVO");
        entity.setRegion(dto.getRegion());
        entity.setCommune(dto.getCommune());
        entity.setMediaUrl(dto.getMediaUrl());

        // JTS Spatial Point creation: X = Longitude, Y = Latitude
        Point point = geometryFactory.createPoint(new Coordinate(dto.getLongitude(), dto.getLatitude()));
        entity.setLocation(point);
        entity.setUserId(dto.getUserId());

        entity = repository.save(entity);
        sendReportNotification(entity);
        return mapToDTO(entity);
    }

    /**
     * Obtiene todos los reportes de incendio.
     */
    public List<FireReportDTO> getAllReports() {
        return repository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    /**
     * Actualiza el estado de un reporte.
     */
    public FireReportDTO updateStatus(Long id, String status) {
        FireReport entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        entity.setStatus(status);
        entity = repository.save(entity);
        return mapToDTO(entity);
    }

    public FireReportDTO updateStatusWithAuthorization(Long id, String status, String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Falta cabecera de Autorización");
        }
        jwtUtil.parseToken(authHeader);
        return updateStatus(id, status);
    }

    private void sendReportNotification(FireReport report) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("title", "Nuevo reporte de incendio");
            payload.put("message", String.format("Se ha reportado un incendio %s en %s.",
                    report.getType() != null ? report.getType().toLowerCase() : "",
                    report.getCommune() != null ? report.getCommune() : "la zona"));
            payload.put("level", "WARNING");
            payload.put("region", report.getRegion());
            payload.put("commune", report.getCommune());
            restTemplate.postForEntity(alertsServiceUrl + "/internal/report-notification", payload, String.class);
        } catch (Exception e) {
            // Do not block report saving if notification cannot be delivered.
            e.printStackTrace();
        }
    }

    /**
     * 
     * 
     * 
     * Invocación del procedimiento almacenado para obtener conteo de reportes
     * activos por tipo.
     */
    @Transactional(readOnly = true)
    public int getActiveReportsCountByType(String type) {
        return repository.getActiveReportsCountByType(type);
    }

    /**
     * Mapea una entidad JPA a DTO.
     */
    private FireReportDTO mapToDTO(FireReport entity) {
        FireReportDTO dto = new FireReportDTO();
        dto.setId(entity.getId());
        dto.setDescription(entity.getDescription());
        dto.setType(entity.getType());
        dto.setStatus(entity.getStatus());
        dto.setLatitude(entity.getLocation().getY());
        dto.setLongitude(entity.getLocation().getX());
        dto.setRegion(entity.getRegion());
        dto.setCommune(entity.getCommune());
        dto.setMediaUrl(entity.getMediaUrl());
        if (entity.getReportDate() != null) {
            dto.setReportDate(entity.getReportDate().toString());
        }
        dto.setUserId(entity.getUserId());
        return dto;
    }
}
