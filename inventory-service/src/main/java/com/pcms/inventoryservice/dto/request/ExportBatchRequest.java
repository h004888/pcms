package com.pcms.inventoryservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ExportBatchRequest(
        @NotNull UUID medicineId,
        @NotNull UUID branchId,
        @NotNull @Min(1) Integer qty,
        @NotBlank String reason,
        UUID actorId
) {}
