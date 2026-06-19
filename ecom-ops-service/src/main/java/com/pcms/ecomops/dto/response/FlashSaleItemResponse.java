package com.pcms.ecomops.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record FlashSaleItemResponse(
        UUID id,
        UUID flashSaleId,
        UUID medicineId,
        BigDecimal originalPrice,
        BigDecimal salePrice,
        Integer qtyLimit,
        Integer soldQty
) {}
