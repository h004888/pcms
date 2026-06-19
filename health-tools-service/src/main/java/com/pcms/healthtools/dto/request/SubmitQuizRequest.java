package com.pcms.healthtools.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

/**
 * Request to submit a quiz.
 */
public record SubmitQuizRequest(
        @NotNull UUID customerId,
        @NotNull Map<String, Object> answers
) {}
