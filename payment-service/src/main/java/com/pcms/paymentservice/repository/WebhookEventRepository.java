package com.pcms.paymentservice.repository;

import com.pcms.paymentservice.entity.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WebhookEventRepository extends JpaRepository<WebhookEvent, UUID> {
    /**
     * Idempotency check: if same gateway event id received, skip.
     */
    Optional<WebhookEvent> findByGatewayEventId(String gatewayEventId);
}
