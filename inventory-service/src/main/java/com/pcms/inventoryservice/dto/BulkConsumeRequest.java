package com.pcms.inventoryservice.dto;

import com.pcms.inventoryservice.dto.request.ConsumeBatchRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Bulk stock-consume payload for saga orchestrator.
 * The orderId on each item identifies the saga aggregate.
 */
public record BulkConsumeRequest(
        @NotEmpty @Valid List<ConsumeBatchRequest> items
) {}