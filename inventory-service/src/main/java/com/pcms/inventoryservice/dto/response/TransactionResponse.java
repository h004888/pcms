package com.pcms.inventoryservice.dto.response;

import com.pcms.inventoryservice.entity.InventoryTransaction;
import com.pcms.inventoryservice.enums.TransactionType;

import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID batchId,
        TransactionType type,
        Integer qty,
        String reason,
        UUID refId,
        UUID actorId,
        LocalDateTime createdAt
) {
    public static TransactionResponse from(InventoryTransaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getBatchId(),
                t.getType(),
                t.getQty(),
                t.getReason(),
                t.getRefId(),
                t.getActorId(),
                t.getCreatedAt()
        );
    }
}
