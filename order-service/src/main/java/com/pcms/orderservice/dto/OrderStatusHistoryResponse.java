package com.pcms.orderservice.dto;

import com.pcms.orderservice.entity.OrderStatusHistory;
import com.pcms.orderservice.enums.OrderStatus;

import java.time.Instant;

public record OrderStatusHistoryResponse(OrderStatus status, Instant occurredAt, String note) {
    public static OrderStatusHistoryResponse from(OrderStatusHistory history) {
        return new OrderStatusHistoryResponse(history.getStatus(), history.getOccurredAt(), history.getNote());
    }
}
