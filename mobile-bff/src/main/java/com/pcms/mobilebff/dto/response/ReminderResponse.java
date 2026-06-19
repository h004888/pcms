package com.pcms.mobilebff.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReminderResponse(
        UUID id,
        UUID customerId,
        UUID familyMemberId,
        String medicineName,
        String dosage,
        String frequency,
        String scheduleTime,
        LocalDate startDate,
        LocalDate endDate,
        Boolean active,
        String notes,
        LocalDateTime createdAt
) {}
