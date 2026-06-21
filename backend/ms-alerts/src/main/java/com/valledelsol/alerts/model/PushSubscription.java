package com.valledelsol.alerts.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "push_subscriptions")
@Data
@NoArgsConstructor
public class PushSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String endpoint;

    @Column(name = "p256dh", nullable = false, columnDefinition = "TEXT")
    private String p256dh;

    @Column(name = "auth", nullable = false, columnDefinition = "TEXT")
    private String auth;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(length = 100)
    private String region;

    @Column(length = 100)
    private String commune;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
