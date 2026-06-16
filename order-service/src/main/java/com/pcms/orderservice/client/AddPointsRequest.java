package com.pcms.orderservice.client;

import java.util.UUID;

/**
 * Request body for {@code PUT /customers/{id}/points/add} (BR07).
 * Sent from order-service / payment-service → customer-service.
 */
public record AddPointsRequest(
        Integer points,
        UUID refOrderId,
        String reason
) {}
