package com.valledelsol.alerts.controller;

import com.valledelsol.alerts.dto.AlertDTO;
import com.valledelsol.alerts.service.AlertService;
import com.valledelsol.alerts.service.PushNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AlertControllerTest {

    @Mock
    private AlertService service;

    @Mock
    private PushNotificationService pushNotificationService;

    @InjectMocks
    private AlertController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateAlertSuccess() {
        AlertDTO input = new AlertDTO();
        input.setTitle("Fire Warning");
        input.setMessage("Evacuate now");
        input.setLevel("DANGER");
        input.setCommune("Valle del Sol");

        AlertDTO saved = new AlertDTO();
        saved.setId(1L);
        saved.setTitle("Fire Warning");
        saved.setLevel("DANGER");

        when(service.createAlertWithAuthorization(any(AlertDTO.class), eq("Bearer admin-token"))).thenReturn(saved);

        ResponseEntity<?> response = controller.createAlert(input, "Bearer admin-token");
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void testCreateAlertAsBrigadist() {
        AlertDTO input = new AlertDTO();
        input.setTitle("Controlled Burn Notice");
        input.setMessage("Sector B under control");
        input.setLevel("WARNING");

        AlertDTO saved = new AlertDTO();
        saved.setId(2L);

        when(service.createAlertWithAuthorization(any(AlertDTO.class), eq("Bearer brigadist-token"))).thenReturn(saved);

        ResponseEntity<?> response = controller.createAlert(input, "Bearer brigadist-token");
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testCreateAlertUnauthorizedNoHeader() {
        AlertDTO input = new AlertDTO();
        when(service.createAlertWithAuthorization(any(AlertDTO.class), isNull()))
                .thenThrow(new IllegalArgumentException("Falta cabecera de Autorización"));

        ResponseEntity<?> response = controller.createAlert(input, null);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testCreateAlertForbiddenRegularUser() {
        AlertDTO input = new AlertDTO();
        when(service.createAlertWithAuthorization(any(AlertDTO.class), eq("Bearer user-token")))
                .thenThrow(new SecurityException("No tienes permisos suficientes para enviar alertas"));

        ResponseEntity<?> response = controller.createAlert(input, "Bearer user-token");
        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void testGetAllAlerts() {
        AlertDTO dto = new AlertDTO();
        dto.setId(1L);
        dto.setTitle("Test");
        when(service.getAllAlerts()).thenReturn(Collections.singletonList(dto));

        ResponseEntity<List<AlertDTO>> response = controller.getAllAlerts();
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testUnsubscribeWithoutEndpoint() {
        ResponseEntity<?> response = controller.unsubscribe(Collections.emptyMap());
        assertEquals(200, response.getStatusCode().value());
        verify(service, never()).addEmitter();
    }

    @Test
    void testUnsubscribeWithEndpoint() {
        controller.unsubscribe(Collections.singletonMap("endpoint", "endpoint-1"));
        verify(pushNotificationService, times(1)).disableSubscription("endpoint-1");
    }

    @Test
    void testStreamAlerts() {
        SseEmitter mockEmitter = new SseEmitter();
        when(service.addEmitter()).thenReturn(mockEmitter);

        SseEmitter result = controller.streamAlerts();
        assertNotNull(result);
        verify(service, times(1)).addEmitter();
    }
}
