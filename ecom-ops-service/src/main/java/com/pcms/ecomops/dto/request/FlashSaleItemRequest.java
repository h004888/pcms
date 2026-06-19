package com.pcms.ecomops.dto.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record FlashSaleItemRequest(
        @NotNull UUID medicineId,
        @NotNull BigDecimal originalPrice,
        @NotNull BigDecimal salePrice,
        @NotNull Integer qtyLimit
) {}
