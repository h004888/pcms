package com.pcms.pharmacistworkbench.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request to schedule a follow-up task (3/7/14-day after purchase).
 */
public record ScheduleFollowUpRequest(
        @NotNull UUID customerId,
        UUID orderId,
        UUID prescriptionId,
        @NotNull Integer daysFromNow,  // 3, 7, 14
        String type  // FEEDBACK, REMINDER, RE_EXAM
) {}
