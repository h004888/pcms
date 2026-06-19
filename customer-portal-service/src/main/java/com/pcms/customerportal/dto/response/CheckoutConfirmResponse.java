package com.pcms.customerportal.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record CheckoutConfirmResponse(
        UUID orderId,
        String orderNumber,
        String paymentUrl,
        BigDecimal total,
        String status
) {}
