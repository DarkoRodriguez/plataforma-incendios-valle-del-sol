package com.valledelsol.alerts.service;

import com.valledelsol.alerts.dto.AlertDTO;
import com.valledelsol.alerts.model.Alert;
import com.valledelsol.alerts.repository.AlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AlertServiceTest {

    @Mock
    private AlertRepository repository;

    @InjectMocks
    private AlertService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createAlertTest() {
        AlertDTO inputDto = new AlertDTO();
        inputDto.setTitle("Fire Alert");
        inputDto.setMessage("Evacuate the high zone of Valle del Sol");
        inputDto.setLevel("DANGER");
        inputDto.setCommune("Valle del Sol");

        Alert savedEntity = new Alert();
        savedEntity.setId(1L);
        savedEntity.setTitle("Fire Alert");
        savedEntity.setMessage("Evacuate the high zone of Valle del Sol");
        savedEntity.setLevel("DANGER");
        savedEntity.setCommune("Valle del Sol");
        savedEntity.setCreatedAt(LocalDateTime.now());

        when(repository.save(any(Alert.class))).thenReturn(savedEntity);

        AlertDTO result = service.createAlert(inputDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("DANGER", result.getLevel());
        assertEquals("Fire Alert", result.getTitle());
        assertEquals("Valle del Sol", result.getCommune());
        assertNotNull(result.getCreatedAt());
        verify(repository, times(1)).save(any(Alert.class));
    }

    @Test
    void createAlertDefaultsLevelToInfoWhenNull() {
        AlertDTO inputDto = new AlertDTO();
        inputDto.setTitle("General Notice");
        inputDto.setMessage("No immediate danger");
        inputDto.setLevel(null);
        inputDto.setCommune(null);

        Alert savedEntity = new Alert();
        savedEntity.setId(2L);
        savedEntity.setTitle("General Notice");
        savedEntity.setMessage("No immediate danger");
        savedEntity.setLevel("INFO");
        savedEntity.setCommune("Valle del Sol");

        when(repository.save(any(Alert.class))).thenReturn(savedEntity);

        AlertDTO result = service.createAlert(inputDto);

        assertNotNull(result);
        assertEquals("INFO", result.getLevel());
        assertEquals("Valle del Sol", result.getCommune());
    }

    @Test
    void getAllAlertsTest() {
        Alert entity1 = new Alert();
        entity1.setId(1L);
        entity1.setTitle("Alert 1");
        entity1.setMessage("Message 1");
        entity1.setLevel("INFO");
        entity1.setCommune("Valle del Sol");

        Alert entity2 = new Alert();
        entity2.setId(2L);
        entity2.setTitle("Alert 2");
        entity2.setMessage("Message 2");
        entity2.setLevel("WARNING");
        entity2.setCommune("Valle del Sol");

        when(repository.findByOrderByCreatedAtDesc()).thenReturn(Arrays.asList(entity1, entity2));

        List<AlertDTO> result = service.getAllAlerts();
        assertEquals(2, result.size());
        assertEquals("Alert 1", result.get(0).getTitle());
        assertEquals("WARNING", result.get(1).getLevel());
    }

    @Test
    void getAllAlertsEmptyListTest() {
        when(repository.findByOrderByCreatedAtDesc()).thenReturn(Collections.emptyList());

        List<AlertDTO> result = service.getAllAlerts();
        assertTrue(result.isEmpty());
    }

    @Test
    void addEmitterReturnsEmitter() {
        SseEmitter emitter = service.addEmitter();
        assertNotNull(emitter);
    }
}
