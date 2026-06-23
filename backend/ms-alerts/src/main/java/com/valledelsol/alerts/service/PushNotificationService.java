package com.valledelsol.alerts.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.valledelsol.alerts.dto.AlertDTO;
import com.valledelsol.alerts.dto.PushSubscriptionDTO;
import com.valledelsol.alerts.model.PushSubscription;
import com.valledelsol.alerts.repository.PushSubscriptionRepository;
import jakarta.annotation.PostConstruct;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Map;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.List;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

@Service
public class PushNotificationService {
    private static final Logger log = LoggerFactory.getLogger(PushNotificationService.class);
    private final PushSubscriptionRepository repository;

    @Value("${vapid.public.key:}")
    private String vapidPublicKey;

    @Value("${vapid.private.key:}")
    private String vapidPrivateKey;

    @Value("${vapid.subject:mailto:admin@example.com}")
    private String vapidSubject;

    private PushService pushService;

    public PushNotificationService(PushSubscriptionRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void init() {
        if (vapidPublicKey == null || vapidPublicKey.isBlank() || vapidPrivateKey == null || vapidPrivateKey.isBlank()) {
            log.warn("VAPID keys are not configured; PushNotificationService will remain disabled.");
            return;
        }

        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }

        try {
            pushService = new PushService(vapidPublicKey, vapidPrivateKey, vapidSubject);
        } catch (GeneralSecurityException e) {
            log.error("Failed to initialize Web Push service; push notifications will be disabled.", e);
            pushService = null;
        } catch (IllegalArgumentException e) {
            log.error("Invalid VAPID key format; push notifications will be disabled.", e);
            pushService = null;
        }
    }

    public void saveSubscription(Long userId, String endpoint, String p256dh, String auth, String region, String commune) {
        PushSubscription existing = repository.findByEndpoint(endpoint);
        if (existing == null) {
            existing = new PushSubscription();
            existing.setEndpoint(endpoint);
        }
        existing.setUserId(userId);
        existing.setP256dh(p256dh);
        existing.setAuth(auth);
        existing.setRegion(region);
        existing.setCommune(commune);
        existing.setEnabled(true);
        repository.save(existing);
    }

    public void disableSubscription(String endpoint) {
        PushSubscription existing = repository.findByEndpoint(endpoint);
        if (existing != null) {
            existing.setEnabled(false);
            repository.save(existing);
        }
    }

    public void registerSubscription(PushSubscriptionDTO dto) {
        if (dto == null || dto.getEndpoint() == null) {
            return;
        }
        saveSubscription(null, dto.getEndpoint(), dto.getP256dh(), dto.getAuth(), dto.getRegion(), dto.getCommune());
    }

    void setPushService(PushService pushService) {
        this.pushService = pushService;
    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public void sendPushNotification(AlertDTO alert) {
        if (pushService == null) {
            return;
        }

        List<PushSubscription> subscriptions = repository.findActiveByCommuneOrRegion(alert.getCommune(), alert.getRegion());
        if (subscriptions.isEmpty()) {
            return;
        }

        try {
            String payload = OBJECT_MAPPER.writeValueAsString(Map.of(
                    "title", alert.getTitle(),
                    "message", alert.getMessage(),
                    "commune", alert.getCommune(),
                    "region", alert.getRegion()
            ));

            for (PushSubscription subscription : subscriptions) {
                try {
                    Notification notification = buildNotification(subscription, payload);
                    // Call send and ignore the concrete HttpResponse type to avoid compile issues
                    // in environments where HttpClient classes are not available on the build path.
                    pushService.send(notification);
                } catch (Exception e) {
                    log.error("Failed to send push notification to {}", subscription.getEndpoint(), e);
                }
            }
        } catch (Exception e) {
            log.error("Failed to serialize push notification payload", e);
        }
    }

    protected Notification buildNotification(PushSubscription subscription, String payload) throws Exception {
        Subscription.Keys keys = new Subscription.Keys(subscription.getP256dh(), subscription.getAuth());
        Subscription webPushSubscription = new Subscription(subscription.getEndpoint(), keys);
        return new Notification(webPushSubscription, payload);
    }
}
