package com.valledelsol.alerts.service;

import com.valledelsol.alerts.auth.JwtUtil;
import com.valledelsol.alerts.dto.AlertDTO;
import com.valledelsol.alerts.model.Alert;
import com.valledelsol.alerts.model.PushSubscription;
import com.valledelsol.alerts.repository.AlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AlertServiceTest {

    @Mock
    private AlertRepository repository;

    @Mock
    private PushNotificationService pushNotificationService;

    @Mock
    private JwtUtil jwtUtil;

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
        verify(pushNotificationService, times(1)).sendPushNotification(any(AlertDTO.class));
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
        verify(pushNotificationService, times(1)).sendPushNotification(any(AlertDTO.class));
    }

    @Test
    void createAlertWithAuthorizationSuccess() {
        AlertDTO inputDto = new AlertDTO();
        inputDto.setTitle("Fire Alert");
        inputDto.setMessage("Evacuate the high zone of Valle del Sol");
        inputDto.setLevel("DANGER");
        inputDto.setCommune("Valle del Sol");

        Alert savedEntity = new Alert();
        savedEntity.setId(3L);
        savedEntity.setTitle("Fire Alert");
        savedEntity.setMessage("Evacuate the high zone of Valle del Sol");
        savedEntity.setLevel("DANGER");
        savedEntity.setCommune("Valle del Sol");
        savedEntity.setCreatedAt(LocalDateTime.now());

        when(jwtUtil.getRole("Bearer admin-token")).thenReturn("ADMINISTRATOR");
        when(repository.save(any(Alert.class))).thenReturn(savedEntity);

        AlertDTO result = service.createAlertWithAuthorization(inputDto, "Bearer admin-token");

        assertNotNull(result);
        assertEquals(3L, result.getId());
        verify(pushNotificationService, times(1)).sendPushNotification(any(AlertDTO.class));
    }

    @Test
    void createAlertWithAuthorizationForbiddenRole() {
        AlertDTO inputDto = new AlertDTO();
        inputDto.setTitle("Info");
        inputDto.setMessage("No danger");

        when(jwtUtil.getRole("Bearer user-token")).thenReturn("USER");

        assertThrows(SecurityException.class,
                () -> service.createAlertWithAuthorization(inputDto, "Bearer user-token"));
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

    @Test
    void createAlertWithAuthorizationMissingHeaderThrows() {
        AlertDTO inputDto = new AlertDTO();
        inputDto.setTitle("Fire Alert");
        inputDto.setMessage("Message");

        assertThrows(IllegalArgumentException.class,
                () -> service.createAlertWithAuthorization(inputDto, null));
    }

    @Test
    void createAlertWithAuthorizationInvalidTokenThrows() {
        AlertDTO inputDto = new AlertDTO();
        inputDto.setTitle("Fire Alert");
        inputDto.setMessage("Message");

        when(jwtUtil.getRole("Bearer invalid-token")).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> service.createAlertWithAuthorization(inputDto, "Bearer invalid-token"));
    }

    @Test
    void testAlertModelPrePersistSetsCreatedAt() throws Exception {
        Alert alert = new Alert();
        var onCreate = Alert.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);
        onCreate.invoke(alert);
        assertNotNull(alert.getCreatedAt());
    }

    @Test
    void testAlertModelPrePersistKeepsExistingCreatedAt() throws Exception {
        Alert alert = new Alert();
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 12, 0);
        alert.setCreatedAt(createdAt);
        var onCreate = Alert.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);
        onCreate.invoke(alert);
        assertEquals(createdAt, alert.getCreatedAt());
    }

    @Test
    void testPushSubscriptionModelPrePersistSetsCreatedAt() throws Exception {
        PushSubscription subscription = new PushSubscription();
        var onCreate = PushSubscription.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);
        onCreate.invoke(subscription);
        assertNotNull(subscription.getCreatedAt());
    }

    @Test
    void testPushSubscriptionModelPrePersistKeepsExistingCreatedAt() throws Exception {
        PushSubscription subscription = new PushSubscription();
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 2, 12, 0);
        subscription.setCreatedAt(createdAt);
        var onCreate = PushSubscription.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);
        onCreate.invoke(subscription);
        assertEquals(createdAt, subscription.getCreatedAt());
    }
}
