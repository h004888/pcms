package com.pcms.customerservice.dto.request;

import java.util.UUID;

public record AddPointsRequest(
        Integer points,
        UUID refOrderId,
        String reason) {
}