package com.pcms.pharmacistworkbench.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record FollowUpTaskResponse(
        UUID id,
        UUID customerId,
        UUID orderId,
        UUID prescriptionId,
        String type,
        LocalDateTime scheduledAt,
        String status,
        String response,
        String note
) {}
