package com.pcms.customerportal.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Body of POST /wallet/redeem.
 * FR14.23 - Ví Khỏe Nhà Ta.
 */
public record RedeemPointsRequest(
        @Min(value = 1, message = "points must be >= 1")
        int points,

        @NotBlank(message = "rewardType is required")
        @Size(max = 50)
        String rewardType
) {}
