package com.pcms.customerportal.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderTrackingResponse(
        UUID orderId,
        String orderNumber,
        String status,
        List<TimelineEntry> timeline
) {
    public record TimelineEntry(
            String status,
            Instant occurredAt,
            String note
    ) {}
}
