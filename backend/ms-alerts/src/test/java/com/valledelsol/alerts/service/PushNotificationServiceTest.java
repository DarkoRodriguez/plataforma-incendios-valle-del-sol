package com.valledelsol.alerts.service;

import com.valledelsol.alerts.dto.AlertDTO;
import com.valledelsol.alerts.dto.PushSubscriptionDTO;
import com.valledelsol.alerts.model.PushSubscription;
import com.valledelsol.alerts.repository.PushSubscriptionRepository;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PushNotificationServiceTest {

    @Mock
    private PushSubscriptionRepository repository;

    private PushNotificationService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = spy(new PushNotificationService(repository));
    }

    @Test
    void testRegisterSubscriptionWithNullEndpointDoesNothing() {
        service.registerSubscription(null);
        verify(repository, never()).save(any());
    }

    @Test
    void testRegisterSubscriptionSavesWhenEndpointPresent() {
        PushSubscriptionDTO dto = new PushSubscriptionDTO();
        dto.setEndpoint("endpoint-1");
        dto.setP256dh("p256");
        dto.setAuth("auth");
        dto.setRegion("region");
        dto.setCommune("commune");

        when(repository.findByEndpoint("endpoint-1")).thenReturn(null);

        service.registerSubscription(dto);

        verify(repository, times(1)).save(any(PushSubscription.class));
    }

    @Test
    void testSaveSubscriptionCreatesNewEntity() {
        when(repository.findByEndpoint("endpoint-1")).thenReturn(null);

        service.saveSubscription(1L, "endpoint-1", "p256", "auth", "region", "commune");

        verify(repository, times(1)).save(any(PushSubscription.class));
    }

    @Test
    void testSaveSubscriptionUpdatesExistingEntity() {
        PushSubscription existing = new PushSubscription();
        existing.setEndpoint("endpoint-1");
        existing.setP256dh("old");
        existing.setAuth("old");
        existing.setEnabled(false);

        when(repository.findByEndpoint("endpoint-1")).thenReturn(existing);

        service.saveSubscription(1L, "endpoint-1", "p256", "auth", "region", "commune");

        assertEquals("p256", existing.getP256dh());
        assertTrue(existing.isEnabled());
        verify(repository, times(1)).save(existing);
    }

    @Test
    void testDisableSubscriptionUpdatesExisting() {
        PushSubscription existing = new PushSubscription();
        existing.setEndpoint("endpoint-2");
        existing.setEnabled(true);
        when(repository.findByEndpoint("endpoint-2")).thenReturn(existing);

        service.disableSubscription("endpoint-2");

        assertFalse(existing.isEnabled());
        verify(repository, times(1)).save(existing);
    }

    @Test
    void testDisableSubscriptionDoesNothingWhenNotFound() {
        when(repository.findByEndpoint("missing")).thenReturn(null);

        service.disableSubscription("missing");

        verify(repository, never()).save(any());
    }

    @Test
    void testSendPushNotificationReturnsWhenPushServiceNotConfigured() {
        AlertDTO alert = new AlertDTO();
        alert.setTitle("Title");
        alert.setMessage("Message");
        alert.setCommune("commune");
        alert.setRegion("region");

        service.sendPushNotification(alert);

        verify(repository, never()).findActiveByCommuneOrRegion(anyString(), anyString());
    }

    @Test
    void testSendPushNotificationSkipsWhenNoSubscriptions() throws Exception {
        AtomicInteger sendCount = new AtomicInteger();
        PushService pushService = new PushService() {
            @Override
            public HttpResponse send(Notification notification) {
                sendCount.incrementAndGet();
                return null;
            }
        };
        service.setPushService(pushService);

        AlertDTO alert = new AlertDTO();
        alert.setTitle("Title");
        alert.setMessage("Message");
        alert.setCommune("commune");
        alert.setRegion("region");

        when(repository.findActiveByCommuneOrRegion("commune", "region")).thenReturn(List.of());

        service.sendPushNotification(alert);

        assertEquals(0, sendCount.get());
    }

    @Test
    void testSendPushNotificationSendsToSubscriptions() throws Exception {
        AtomicInteger sendCount = new AtomicInteger();
        PushService pushService = new PushService() {
            @Override
            public HttpResponse send(Notification notification) {
                sendCount.incrementAndGet();
                return null;
            }
        };
        service.setPushService(pushService);
        doReturn(mock(Notification.class)).when(service).buildNotification(any(PushSubscription.class), anyString());

        PushSubscription subscription = new PushSubscription();
        subscription.setEndpoint("endpoint-1");
        subscription.setP256dh("p256");
        subscription.setAuth("auth");
        subscription.setCommune("commune");
        subscription.setRegion("region");
        subscription.setEnabled(true);

        AlertDTO alert = new AlertDTO();
        alert.setTitle("Title");
        alert.setMessage("Message");
        alert.setCommune("commune");
        alert.setRegion("region");

        when(repository.findActiveByCommuneOrRegion("commune", "region")).thenReturn(List.of(subscription));

        service.sendPushNotification(alert);

        assertEquals(1, sendCount.get());
        verify(service, times(1)).buildNotification(any(PushSubscription.class), anyString());
    }

    @Test
    void testSendPushNotificationContinuesWhenSendFails() throws Exception {
        AtomicInteger sendCount = new AtomicInteger();
        PushService pushService = new PushService() {
            @Override
            public HttpResponse send(Notification notification) {
                sendCount.incrementAndGet();
                throw new RuntimeException("failure");
            }
        };
        service.setPushService(pushService);
        doReturn(mock(Notification.class)).when(service).buildNotification(any(PushSubscription.class), anyString());

        PushSubscription subscription = new PushSubscription();
        subscription.setEndpoint("endpoint-1");
        subscription.setP256dh("p256");
        subscription.setAuth("auth");
        subscription.setCommune("commune");
        subscription.setRegion("region");
        subscription.setEnabled(true);

        AlertDTO alert = new AlertDTO();
        alert.setTitle("Title");
        alert.setMessage("Message");
        alert.setCommune("commune");
        alert.setRegion("region");

        when(repository.findActiveByCommuneOrRegion("commune", "region")).thenReturn(List.of(subscription));

        assertDoesNotThrow(() -> service.sendPushNotification(alert));
        assertEquals(1, sendCount.get());
    }

}
