package com.pcms.common.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Base record for all domain events published on the event bus (CR-08).
 *
 * <p>All PCMS domain events MUST extend this contract so that consumers
 * (notification-service, report-service, etc.) can rely on a stable envelope.
 *
 * <p>Fields:
 * <ul>
 *   <li>{@code eventId}      — unique id for this event (idempotency key for consumers)</li>
 *   <li>{@code eventType}    — fully-qualified type, e.g. {@code ORDER_PAID}</li>
 *   <li>{@code occurredAt}   — server-side UTC instant when event was emitted</li>
 *   <li>{@code source}       — producing service, e.g. {@code order-service}</li>
 *   <li>{@code aggregateId}  — id of the aggregate (order-uuid, payment-uuid, ...)</li>
 *   <li>{@code correlationId}— request correlation id (CR-06)</li>
 *   <li>{@code payload}      — event-specific data, JSON-serializable Map</li>
 * </ul>
 *
 * <p>Example topics (Kafka):
 * <ul>
 *   <li>{@code pcms.order.paid}</li>
 *   <li>{@code pcms.inventory.low_stock}</li>
 *   <li>{@code pcms.inventory.expiry}</li>
 *   <li>{@code pcms.payment.success}</li>
 *   <li>{@code pcms.notification.requested}</li>
 * </ul>
 */
public record DomainEvent(
        String eventId,
        String eventType,
        Instant occurredAt,
        String source,
        String aggregateId,
        String correlationId,
        Map<String, Object> payload
) {
    public static final String EVENT_TYPE_ORDER_PAID = "ORDER_PAID";
    public static final String EVENT_TYPE_ORDER_CANCELLED = "ORDER_CANCELLED";
    public static final String EVENT_TYPE_PAYMENT_SUCCESS = "PAYMENT_SUCCESS";
    public static final String EVENT_TYPE_PAYMENT_FAILED = "PAYMENT_FAILED";
    public static final String EVENT_TYPE_INVENTORY_LOW_STOCK = "INVENTORY_LOW_STOCK";
    public static final String EVENT_TYPE_INVENTORY_EXPIRY = "INVENTORY_EXPIRY";
    public static final String EVENT_TYPE_NOTIFICATION_REQUESTED = "NOTIFICATION_REQUESTED";
    public static final String EVENT_TYPE_PRESCRIPTION_ISSUED = "PRESCRIPTION_ISSUED";
    public static final String EVENT_TYPE_CUSTOMER_REGISTERED = "CUSTOMER_REGISTERED";

    public static DomainEvent of(String type, String source, String aggregateId,
                                 String correlationId, Map<String, Object> payload) {
        return new DomainEvent(
                UUID.randomUUID().toString(),
                type,
                Instant.now(),
                source,
                aggregateId,
                correlationId,
                payload
        );
    }
}
