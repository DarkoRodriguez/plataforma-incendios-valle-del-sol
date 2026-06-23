package com.valledelsol.reports.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.valledelsol.reports.dto.FireReportDTO;
import com.valledelsol.reports.service.FireReportService;
import com.valledelsol.reports.service.MinioService;

import java.util.List;

/**
 * Controlador REST encargado de exponer y gestionar los reportes de incendios.
 */
@RestController
@RequestMapping("/api/reports")
public class FireReportController {

    @Autowired
    private FireReportService service;

    @Autowired
    private MinioService minioService;


    /**
     * Endpoint para reportar un nuevo incendio mediante JSON (sin archivos).
     */
    @PostMapping
    public ResponseEntity<FireReportDTO> createReport(@RequestBody FireReportDTO dto) {
        return ResponseEntity.ok(service.createReport(dto));
    }

    /**
     * 
     * 
     * 
     * Endpoint para reportar un nuevo incendio incluyendo foto o video
     * (multipart/form-data).
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FireReportDTO> createReportWithFile(
            @RequestParam("description") String description,
            @RequestParam("type") String type,
            @RequestParam("status") String status,
            @RequestParam("latitude") double latitude, @RequestParam("longitude") double longitude,
            @RequestParam(value = "region", required = false) String region,
            @RequestParam(value = "commune", required = false) String commune,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        String mediaUrl = null;
        if (file != null && !file.isEmpty()) {
            mediaUrl = minioService.uploadFile(file);
        }

        FireReportDTO dto = new FireReportDTO();
        dto.setDescription(description);
        dto.setType(type);
        dto.setStatus(status);
        dto.setLatitude(latitude);
        dto.setLongitude(longitude);
        dto.setRegion(region);
        dto.setCommune(commune);
        dto.setUserId(userId);
        dto.setMediaUrl(mediaUrl);

        return ResponseEntity.ok(service.createReport(dto));
    }

    /**
     * Endpoint para obtener la lista de todos los reportes.
     */
    @GetMapping
    public ResponseEntity<List<FireReportDTO>> getAllReports() {
        return ResponseEntity.ok(service.getAllReports());
    }

    /**
     * Endpoint para actualizar el estado operativo de un incendio (ACTIVO, CONTROLADO, EXTINGUIDO).
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestBody StatusRequest req,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        try {
            FireReportDTO updated = service.updateStatusWithAuthorization(id, req.status, authHeader);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Token inválido o expirado");
        }
    }

    /**
     * Endpoint que expone la consulta por procedimiento almacenado.
     */
    @GetMapping("/statistics/count")
    public ResponseEntity<Integer> getActiveCountByType(@RequestParam("type") String type) {
        int count = service.getActiveReportsCountByType(type);
        return ResponseEntity.ok(count);
    }

    static class StatusRequest {
        public String status;
    }
}
