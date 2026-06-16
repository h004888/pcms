package com.pcms.common.event;

import java.util.Map;
import java.util.UUID;

/**
 * Typed convenience builders for the most common PCMS domain events (CR-08).
 *
 * <p>Use these helpers instead of constructing raw {@link DomainEvent} instances
 * so the {@code eventType} and {@code payload} shape stay consistent across producers.
 *
 * <p>Example:
 * <pre>{@code
 * DomainEvent evt = DomainEvents.orderPaid(orderId, customerId, total, branchId, correlationId);
 * eventPublisher.publish("pcms.order.paid", evt);
 * }</pre>
 */
public final class DomainEvents {

    private DomainEvents() {
        // utility
    }

    public static DomainEvent orderPaid(UUID orderId, UUID customerId, java.math.BigDecimal total,
                                        UUID branchId, String correlationId) {
        return DomainEvent.of(
                DomainEvent.EVENT_TYPE_ORDER_PAID,
                "order-service",
                orderId.toString(),
                correlationId,
                Map.of(
                        "orderId", orderId.toString(),
                        "customerId", customerId.toString(),
                        "total", total,
                        "branchId", branchId.toString()
                )
        );
    }

    public static DomainEvent orderCancelled(UUID orderId, UUID customerId, String reason, String correlationId) {
        return DomainEvent.of(
                DomainEvent.EVENT_TYPE_ORDER_CANCELLED,
                "order-service",
                orderId.toString(),
                correlationId,
                Map.of(
                        "orderId", orderId.toString(),
                        "customerId", customerId.toString(),
                        "reason", reason
                )
        );
    }

    public static DomainEvent paymentSuccess(UUID paymentId, UUID orderId, java.math.BigDecimal amount,
                                            String method, String correlationId) {
        return DomainEvent.of(
                DomainEvent.EVENT_TYPE_PAYMENT_SUCCESS,
                "payment-service",
                paymentId.toString(),
                correlationId,
                Map.of(
                        "paymentId", paymentId.toString(),
                        "orderId", orderId.toString(),
                        "amount", amount,
                        "method", method
                )
        );
    }

    public static DomainEvent inventoryLowStock(UUID medicineId, UUID branchId,
                                                int currentQty, int minQty, String correlationId) {
        return DomainEvent.of(
                DomainEvent.EVENT_TYPE_INVENTORY_LOW_STOCK,
                "inventory-service",
                medicineId.toString() + "@" + branchId.toString(),
                correlationId,
                Map.of(
                        "medicineId", medicineId.toString(),
                        "branchId", branchId.toString(),
                        "currentQty", currentQty,
                        "minQty", minQty
                )
        );
    }

    public static DomainEvent inventoryExpiry(UUID batchId, UUID medicineId, UUID branchId,
                                              String batchNo, String expiryDate, int daysUntilExpiry,
                                              String correlationId) {
        return DomainEvent.of(
                DomainEvent.EVENT_TYPE_INVENTORY_EXPIRY,
                "inventory-service",
                batchId.toString(),
                correlationId,
                Map.of(
                        "batchId", batchId.toString(),
                        "medicineId", medicineId.toString(),
                        "branchId", branchId.toString(),
                        "batchNo", batchNo,
                        "expiryDate", expiryDate,
                        "daysUntilExpiry", daysUntilExpiry
                )
        );
    }

    public static DomainEvent notificationRequested(UUID recipientId, String channel,
                                                    String template, String correlationId) {
        return DomainEvent.of(
                DomainEvent.EVENT_TYPE_NOTIFICATION_REQUESTED,
                "system",
                recipientId.toString(),
                correlationId,
                Map.of(
                        "recipientId", recipientId.toString(),
                        "channel", channel,
                        "template", template
                )
        );
    }

    public static DomainEvent prescriptionIssued(UUID prescriptionId, UUID patientId,
                                                 UUID doctorId, String correlationId) {
        return DomainEvent.of(
                DomainEvent.EVENT_TYPE_PRESCRIPTION_ISSUED,
                "prescription-service",
                prescriptionId.toString(),
                correlationId,
                Map.of(
                        "prescriptionId", prescriptionId.toString(),
                        "patientId", patientId.toString(),
                        "doctorId", doctorId.toString()
                )
        );
    }

    public static DomainEvent customerRegistered(UUID customerId, String code, String correlationId) {
        return DomainEvent.of(
                DomainEvent.EVENT_TYPE_CUSTOMER_REGISTERED,
                "customer-service",
                customerId.toString(),
                correlationId,
                Map.of(
                        "customerId", customerId.toString(),
                        "code", code
                )
        );
    }
}
