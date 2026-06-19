package com.pcms.mobilebff.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Request to create a new medication reminder.
 */
public record CreateReminderRequest(
        @NotNull UUID customerId,
        UUID familyMemberId,
        @NotBlank String medicineName,
        String dosage,
        @NotBlank String frequency,  // MORNING/NOON/EVENING/NIGHT/CUSTOM
        String scheduleTime,  // "08:00,12:00,20:00"
        @NotNull LocalDate startDate,
        LocalDate endDate,
        String notes
) {}
