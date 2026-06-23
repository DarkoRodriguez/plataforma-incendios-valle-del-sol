package com.valledelsol.alerts.repository;

import com.valledelsol.alerts.model.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {

    PushSubscription findByEndpoint(String endpoint);

    @Query("SELECT p FROM PushSubscription p WHERE p.enabled = true AND (lower(p.commune) = lower(?1) OR lower(p.region) = lower(?2))")
    List<PushSubscription> findActiveByCommuneOrRegion(String commune, String region);
}
