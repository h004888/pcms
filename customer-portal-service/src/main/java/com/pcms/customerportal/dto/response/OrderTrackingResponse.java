package com.pcms.customerportal.dto.response;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderTrackingResponse(
        UUID orderId,
        String orderNumber,
        String status,
        List<TimelineEntry> timeline,
        String currentLocation,
        Instant estimatedDelivery
) {
    public record TimelineEntry(
            String status,
            Instant timestamp,
            String note,
            String location
    ) {}
}
