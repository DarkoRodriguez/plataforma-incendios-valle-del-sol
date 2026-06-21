package com.valledelsol.reports.service;

import com.valledelsol.reports.dto.FireReportDTO;
import com.valledelsol.reports.model.FireReport;
import com.valledelsol.reports.repository.FireReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FireReportServiceTest {

    @Mock
    private FireReportRepository repository;

    @Mock
    private MinioService minioService;

    @InjectMocks
    private FireReportService service;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createReportTest() {
        FireReportDTO inputDto = new FireReportDTO();
        inputDto.setDescription("Wildfire near valley");
        inputDto.setType("FORESTAL");
        inputDto.setStatus("ACTIVE");
        inputDto.setRegion("Valparaiso");
        inputDto.setCommune("Quilpué");
        inputDto.setLatitude(-33.45);
        inputDto.setLongitude(-70.66);

        FireReport savedEntity = new FireReport();
        savedEntity.setId(1L);
        savedEntity.setDescription("Wildfire near valley");
        savedEntity.setType("FORESTAL");
        savedEntity.setStatus("ACTIVE");
        savedEntity.setRegion("Valparaiso");
        savedEntity.setCommune("Quilpué");
        savedEntity.setLocation(geometryFactory.createPoint(new Coordinate(-70.66, -33.45)));

        when(repository.save(any(FireReport.class))).thenReturn(savedEntity);

        FireReportDTO result = service.createReport(inputDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("FORESTAL", result.getType());
        assertEquals("ACTIVE", result.getStatus());
        assertEquals("Valparaiso", result.getRegion());
        assertEquals("Quilpué", result.getCommune());
        assertEquals(-33.45, result.getLatitude());
        assertEquals(-70.66, result.getLongitude());
    }

    @Test
    void getAllReportsTest() {
        FireReport entity = new FireReport();
        entity.setId(1L);
        entity.setDescription("Test");
        entity.setType("FORESTAL");
        entity.setStatus("ACTIVE");
        entity.setLocation(geometryFactory.createPoint(new Coordinate(-70.66, -33.45)));

        when(repository.findAll()).thenReturn(Collections.singletonList(entity));

        List<FireReportDTO> result = service.getAllReports();
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void updateStatusTest() {
        FireReport entity = new FireReport();
        entity.setId(1L);
        entity.setDescription("Test");
        entity.setType("FORESTAL");
        entity.setStatus("ACTIVE");
        entity.setLocation(geometryFactory.createPoint(new Coordinate(-70.66, -33.45)));

        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(any(FireReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FireReportDTO result = service.updateStatus(1L, "CONTROLLED");

        assertNotNull(result);
        assertEquals("CONTROLLED", result.getStatus());
        verify(repository, times(1)).save(any(FireReport.class));
    }

    @Test
    void getActiveReportsCountByTypeTest() {
        when(repository.getActiveReportsCountByType("FORESTAL")).thenReturn(5);

        int count = service.getActiveReportsCountByType("FORESTAL");

        assertEquals(5, count);
        verify(repository, times(1)).getActiveReportsCountByType("FORESTAL");
}
}
