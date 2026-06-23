package com.valledelsol.reports.controller;

import com.valledelsol.reports.dto.FireReportDTO;
import com.valledelsol.reports.service.FireReportService;
import com.valledelsol.reports.service.MinioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class FireReportControllerTest {

    @Mock
    private FireReportService service;

    @Mock
    private MinioService minioService;

    @InjectMocks
    private FireReportController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateReport() {
        FireReportDTO dto = new FireReportDTO();
        dto.setId(1L);
        when(service.createReport(any(FireReportDTO.class))).thenReturn(dto);

        ResponseEntity<FireReportDTO> response = controller.createReport(dto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void testCreateReportWithFile() {
        MockMultipartFile file = new MockMultipartFile("file", "report.jpg", "image/jpeg", "data".getBytes());
        when(minioService.uploadFile(file)).thenReturn("https://minio/reports/report.jpg");

        FireReportDTO dto = new FireReportDTO();
        dto.setId(2L);
        when(service.createReport(any(FireReportDTO.class))).thenReturn(dto);

        ResponseEntity<FireReportDTO> response = controller.createReportWithFile(
                "desc", "FORESTAL", "ACTIVE", -33.45, -70.66,
                "Valparaiso", "Quilpué", 1L, file);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(2L, response.getBody().getId());
    }

    @Test
    void testGetAllReports() {
        FireReportDTO dto = new FireReportDTO();
        dto.setId(3L);
        when(service.getAllReports()).thenReturn(List.of(dto));

        ResponseEntity<List<FireReportDTO>> response = controller.getAllReports();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testUpdateStatusUnauthorized() {
        FireReportController.StatusRequest request = new FireReportController.StatusRequest();
        request.status = "CONTROLLED";
        when(service.updateStatusWithAuthorization(eq(1L), eq("CONTROLLED"), any()))
                .thenThrow(new IllegalArgumentException("Falta cabecera de Autorización"));

        ResponseEntity<?> response = controller.updateStatus(1L, request, null);

        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetActiveCountByType() {
        when(service.getActiveReportsCountByType("FORESTAL")).thenReturn(5);

        ResponseEntity<Integer> response = controller.getActiveCountByType("FORESTAL");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(5, response.getBody());
    }

    @Test
    void testCreateReportWithFileEmptyFileDoesNotUpload() {
        MockMultipartFile file = new MockMultipartFile("file", "report.jpg", "image/jpeg", new byte[0]);
        FireReportDTO dto = new FireReportDTO();
        dto.setId(4L);
        when(service.createReport(any(FireReportDTO.class))).thenReturn(dto);

        ResponseEntity<FireReportDTO> response = controller.createReportWithFile(
                "desc", "FORESTAL", "ACTIVE", -33.45, -70.66,
                "Valparaiso", "Quilpué", 1L, file);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(4L, response.getBody().getId());
        verify(minioService, never()).uploadFile(any());
    }

    @Test
    void testUpdateStatusReturnsUnauthorizedOnGenericError() {
        FireReportController.StatusRequest request = new FireReportController.StatusRequest();
        request.status = "CONTROLLED";
        when(service.updateStatusWithAuthorization(eq(1L), eq("CONTROLLED"), any()))
                .thenThrow(new RuntimeException("boom"));

        ResponseEntity<?> response = controller.updateStatus(1L, request, "Bearer token");

        assertEquals(401, response.getStatusCode().value());
        assertEquals("Token inválido o expirado", response.getBody());
    }
}
