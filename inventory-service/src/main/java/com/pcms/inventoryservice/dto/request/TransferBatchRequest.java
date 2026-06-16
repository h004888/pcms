package com.pcms.inventoryservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TransferBatchRequest(
        @NotNull UUID medicineId,
        @NotNull UUID fromBranchId,
        @NotNull UUID toBranchId,
        @NotNull @Min(1) Integer qty,
        @NotBlank String reason,
        UUID actorId
) {}
