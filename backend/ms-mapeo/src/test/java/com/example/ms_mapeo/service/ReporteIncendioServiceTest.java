package com.example.ms_mapeo.service;

import com.example.ms_mapeo.dto.ReporteIncendioDTO;
import com.example.ms_mapeo.model.ReporteIncendio;
import com.example.ms_mapeo.repository.ReporteIncendioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ReporteIncendioServiceTest {

    @Mock
    private ReporteIncendioRepository repository;

    @InjectMocks
    private ReporteIncendioService service;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void crearReporteTest() {
        ReporteIncendioDTO inputDto = new ReporteIncendioDTO();
        inputDto.setDescripcion("Incendio en bosque");
        inputDto.setLatitud(-33.45);
        inputDto.setLongitud(-70.66);

        ReporteIncendio savedEntity = new ReporteIncendio();
        savedEntity.setId(1L);
        savedEntity.setDescripcion("Incendio en bosque");
        savedEntity.setTipo("FORESTAL");
        savedEntity.setEstado("ACTIVO");
        savedEntity.setUbicacion(geometryFactory.createPoint(new Coordinate(-70.66, -33.45)));

        when(repository.save(any(ReporteIncendio.class))).thenReturn(savedEntity);

        ReporteIncendioDTO result = service.crearReporte(inputDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("FORESTAL", result.getTipo());
        assertEquals(-33.45, result.getLatitud());
    }

    @Test
    void obtenerTodosTest() {
        ReporteIncendio entity = new ReporteIncendio();
        entity.setId(1L);
        entity.setDescripcion("Test");
        entity.setUbicacion(geometryFactory.createPoint(new Coordinate(0, 0)));

        when(repository.findAll()).thenReturn(Collections.singletonList(entity));

        List<ReporteIncendioDTO> result = service.obtenerTodos();
        assertEquals(1, result.size());
    }
}
