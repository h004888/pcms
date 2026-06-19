package com.pcms.healthtools.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record QuizResultResponse(
        UUID id,
        UUID customerId,
        String quizSlug,
        Integer score,
        String riskLevel,
        String advice,
        LocalDateTime completedAt
) {}
