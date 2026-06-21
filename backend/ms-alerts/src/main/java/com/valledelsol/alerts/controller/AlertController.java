package com.valledelsol.alerts.controller;

import com.valledelsol.alerts.dto.AlertDTO;
import com.valledelsol.alerts.dto.PushSubscriptionDTO;
import com.valledelsol.alerts.service.AlertService;
import com.valledelsol.alerts.service.PushNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    @Autowired
    private AlertService service;

    @Autowired
    private PushNotificationService pushNotificationService;

    /**
     * Publica una nueva alerta comunal.
     * Restringido a BRIGADIST y ADMINISTRATOR.
     */
    @PostMapping
    public ResponseEntity<?> createAlert(
            @RequestBody AlertDTO dto,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        try {
            AlertDTO created = service.createAlertWithAuthorization(dto, authHeader);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    /**
     * Obtiene el listado completo de alertas (historial).
     */
    @PostMapping("/subscriptions")
    public ResponseEntity<?> registerSubscription(@RequestBody PushSubscriptionDTO subscription) {
        pushNotificationService.registerSubscription(subscription);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/internal/report-notification")
    public ResponseEntity<?> notifyReport(@RequestBody AlertDTO alertPayload) {
        service.createAlert(alertPayload);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<AlertDTO>> getAllAlerts() {
        return ResponseEntity.ok(service.getAllAlerts());
    }

    /**
     * Establece la conexión de transmisión de eventos en tiempo real (SSE).
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAlerts() {
        return service.addEmitter();
    }
}
