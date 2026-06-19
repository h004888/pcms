package com.pcms.pharmacistworkbench.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response for consultation endpoints.
 */
public record ConsultationResponse(
        UUID id,
        UUID customerId,
        UUID pharmacistId,
        String channel,
        String status,
        LocalDateTime startedAt,
        LocalDateTime endedAt
) {}
