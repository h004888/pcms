package com.pcms.inventoryservice.service;

import com.pcms.inventoryservice.dto.request.ConsumeBatchRequest;

import java.util.UUID;

public interface OutboxConsumerService {

    Object handleOrderPaid(UUID orderId, UUID eventId, ConsumeBatchRequest request);

    Object handleOrderCancelled(UUID orderId, UUID eventId, ConsumeBatchRequest request);
}