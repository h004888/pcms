package com.pcms.orderservice.dto;

import com.pcms.orderservice.enums.CouponType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateCouponRequest(
        @NotBlank(message = "Mã coupon không được để trống") @Size(max = 30, message = "Mã coupon không được vượt quá 30 ký tự") String code,

        @NotBlank(message = "Mô tả coupon không được để trống") @Size(max = 255, message = "Mô tả coupon không được vượt quá 255 ký tự") String description,

        @NotNull(message = "Loại coupon không được để trống") CouponType couponType,

        @NotNull(message = "Giá trị coupon không được để trống") @DecimalMin(value = "0.01", message = "Giá trị coupon phải lớn hơn 0") BigDecimal value,

        @NotNull(message = "Ngày bắt đầu không được để trống") LocalDateTime validFrom,

        @NotNull(message = "Ngày kết thúc không được để trống") LocalDateTime validTo,
        Integer maxUses) {
}