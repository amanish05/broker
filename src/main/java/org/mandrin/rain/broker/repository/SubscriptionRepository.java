package org.mandrin.rain.broker.repository;

import org.mandrin.rain.broker.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    boolean existsByInstrumentToken(Long instrumentToken);
}
