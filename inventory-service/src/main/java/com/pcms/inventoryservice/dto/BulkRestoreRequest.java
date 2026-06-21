package com.pcms.inventoryservice.dto;

import com.pcms.inventoryservice.dto.request.ConsumeBatchRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Bulk stock-restore payload for saga compensation.
 * Items must match the original consume: same medicineId/branchId/qty,
 * looked up via InventoryTransaction.refId.
 */
public record BulkRestoreRequest(
        @NotEmpty @Valid List<ConsumeBatchRequest> items
) {}