package com.pcms.paymentservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Request payload for full or partial refund flow (BR-08). */
public record RefundPaymentRequest(
        @DecimalMin(value = "0.01", message = "Số tiền hoàn phải lớn hơn 0") BigDecimal amount,

        @Size(max = 255, message = "Lý do hoàn tiền không được vượt quá 255 ký tự") String reason) {
}