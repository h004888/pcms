package com.pcms.inventoryservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ConsumeBatchRequest(
        @NotNull UUID medicineId,
        @NotNull UUID branchId,
        @NotNull @Min(1) Integer qty,
        UUID actorId,
        UUID orderId
) {}
