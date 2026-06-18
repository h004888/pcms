package com.pcms.inventoryservice.service.impl;

import com.pcms.inventoryservice.dto.request.ConsumeBatchRequest;
import com.pcms.inventoryservice.dto.response.StockOperationResult;
import com.pcms.inventoryservice.entity.OutboxLog;
import com.pcms.inventoryservice.repository.OutboxLogRepository;
import com.pcms.inventoryservice.service.InventoryService;
import com.pcms.inventoryservice.service.OutboxConsumerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class OutboxConsumerServiceImpl implements OutboxConsumerService {

    private final InventoryService inventoryService;
    private final OutboxLogRepository outboxLogRepository;

    public OutboxConsumerServiceImpl(InventoryService inventoryService, OutboxLogRepository outboxLogRepository) {
        this.inventoryService = inventoryService;
        this.outboxLogRepository = outboxLogRepository;
    }

    @Override
    public Object handleOrderPaid(UUID orderId, UUID eventId, ConsumeBatchRequest request) {
        UUID effectiveEventId = eventId != null ? eventId : orderId;
        if (outboxLogRepository.existsByEventId(effectiveEventId)) {
            return Map.of("status", "duplicate", "eventId", effectiveEventId);
        }

        StockOperationResult result = inventoryService.consumeStock(new ConsumeBatchRequest(
                request.medicineId(), request.branchId(), request.qty(), request.actorId(), orderId));
        outboxLogRepository.save(new OutboxLog(effectiveEventId, "ORDER_PAID_STOCK_CONSUME", "PROCESSED", orderId));
        return result;
    }

    @Override
    public Object handleOrderCancelled(UUID orderId, UUID eventId, ConsumeBatchRequest request) {
        UUID effectiveEventId = eventId != null ? eventId : orderId;
        if (outboxLogRepository.existsByEventId(effectiveEventId)) {
            return Map.of("status", "duplicate", "eventId", effectiveEventId);
        }

        StockOperationResult result = inventoryService.restoreStock(new ConsumeBatchRequest(
                request.medicineId(), request.branchId(), request.qty(), request.actorId(), orderId));
        outboxLogRepository
                .save(new OutboxLog(effectiveEventId, "ORDER_CANCELLED_STOCK_RESTORE", "PROCESSED", orderId));
        return result;
    }
}