package com.pcms.customerportal.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderTrackingResponse(
        UUID orderId,
        String orderNumber,
        String status,
        List<TimelineEntry> timeline,
        String currentLocation,
        LocalDateTime estimatedDelivery
) {
    public record TimelineEntry(
            String status,
            LocalDateTime timestamp,
            String note
    ) {}
}
