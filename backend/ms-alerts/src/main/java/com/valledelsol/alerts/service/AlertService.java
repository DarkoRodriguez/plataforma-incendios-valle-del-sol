package com.valledelsol.alerts.service;

import com.valledelsol.alerts.dto.AlertDTO;
import com.valledelsol.alerts.model.Alert;
import com.valledelsol.alerts.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.valledelsol.alerts.auth.JwtUtil;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class AlertService {

    @Autowired
    private AlertRepository repository;

    @Autowired
    private PushNotificationService pushNotificationService;

    @Autowired
    private JwtUtil jwtUtil;

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    /**
     * Crea una alerta, la persiste en BD y la transmite a todos los emisores SSE activos.
     */
    public AlertDTO createAlert(AlertDTO dto) {
        Alert entity = new Alert();
        entity.setTitle(dto.getTitle());
        entity.setMessage(dto.getMessage());
        entity.setLevel(dto.getLevel() != null ? dto.getLevel().toUpperCase() : "INFO");
        entity.setCommune(dto.getCommune() != null ? dto.getCommune() : "Valle del Sol");
        entity.setRegion(dto.getRegion());

        entity = repository.save(entity);
        AlertDTO savedDto = mapToDTO(entity);

        // Transmitir la alerta en tiempo real a los clientes conectados
        broadcast(savedDto);
        pushNotificationService.sendPushNotification(savedDto);

        return savedDto;
    }

    public AlertDTO createAlertWithAuthorization(AlertDTO dto, String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Falta cabecera de Autorización");
        }

        String role = jwtUtil.getRole(authHeader);
        if (role == null) {
            throw new IllegalArgumentException("Token inválido o expirado");
        }

        if (!role.equalsIgnoreCase("BRIGADIST") && !role.equalsIgnoreCase("ADMINISTRATOR")) {
            throw new SecurityException("No tienes permisos suficientes para enviar alertas");
        }

        return createAlert(dto);
    }

    /**
     * Obtiene el listado histórico de alertas ordenadas por fecha descendente.
     */
    public List<AlertDTO> getAllAlerts() {
        return repository.findByOrderByCreatedAtDesc().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Crea y registra un emisor de eventos SSE para un nuevo cliente.
     */
    public SseEmitter addEmitter() {
        // Emitter con tiempo de expiración largo (e.g. 30 minutos)
        SseEmitter emitter = new SseEmitter(1800000L);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> {
            emitter.complete();
            emitters.remove(emitter);
        });
        emitter.onError((ex) -> emitters.remove(emitter));

        // Enviar un evento inicial de conexión para evitar timeout inicial
        try {
            emitter.send(SseEmitter.event()
                    .name("INIT")
                    .data("Connected to Valle del Sol alert channel"));
        } catch (IOException e) {
            emitters.remove(emitter);
        }

        return emitter;
    }

    private void broadcast(AlertDTO alert) {
        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("ALERT")
                        .data(alert));
            } catch (Exception e) {
                deadEmitters.add(emitter);
            }
        }
        emitters.removeAll(deadEmitters);
    }

    private AlertDTO mapToDTO(Alert entity) {
        AlertDTO dto = new AlertDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setMessage(entity.getMessage());
        dto.setLevel(entity.getLevel());
        dto.setCommune(entity.getCommune());
        dto.setRegion(entity.getRegion());
        if (entity.getCreatedAt() != null) {
            dto.setCreatedAt(entity.getCreatedAt().toString());
        }
        return dto;
    }
}
