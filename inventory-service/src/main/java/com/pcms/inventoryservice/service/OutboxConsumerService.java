package com.pcms.inventoryservice.service;

import com.pcms.inventoryservice.dto.BulkConsumeRequest;
import com.pcms.inventoryservice.dto.BulkRestoreRequest;
import com.pcms.inventoryservice.dto.request.ConsumeBatchRequest;

import java.util.UUID;

public interface OutboxConsumerService {

    Object handleOrderPaid(UUID orderId, UUID eventId, ConsumeBatchRequest request);

    Object handleOrderCancelled(UUID orderId, UUID eventId, ConsumeBatchRequest request);

    Object handleOrderPaidBulk(UUID orderId, UUID eventId, BulkConsumeRequest request);

    Object handleOrderCancelledBulk(UUID orderId, UUID eventId, BulkRestoreRequest request);

    Object handleOrderCancelledPrecise(UUID orderId, UUID eventId);
}