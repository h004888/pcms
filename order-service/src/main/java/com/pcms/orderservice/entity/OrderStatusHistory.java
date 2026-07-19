package com.pcms.orderservice.entity;

import com.pcms.orderservice.enums.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "order_status_history", indexes = {
        @Index(name = "idx_order_status_history_order_occurred", columnList = "order_id, occurred_at")
})
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "actor_id")
    private UUID actorId;

    @Column(length = 500)
    private String note;

    public OrderStatusHistory() {
    }

    public OrderStatusHistory(UUID orderId, OrderStatus status, Instant occurredAt, UUID actorId, String note) {
        this.orderId = orderId;
        this.status = status;
        this.occurredAt = occurredAt;
        this.actorId = actorId;
        this.note = note;
    }

    public UUID getId() { return id; }
    public UUID getOrderId() { return orderId; }
    public OrderStatus getStatus() { return status; }
    public Instant getOccurredAt() { return occurredAt; }
    public UUID getActorId() { return actorId; }
    public String getNote() { return note; }
}
