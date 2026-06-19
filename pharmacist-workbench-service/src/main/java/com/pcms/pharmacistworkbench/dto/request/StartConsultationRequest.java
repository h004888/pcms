package com.pcms.pharmacistworkbench.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request to start a new consultation.
 */
public record StartConsultationRequest(
        @NotNull UUID customerId,
        @NotBlank String channel // TEXT, VOICE, VIDEO
) {}
