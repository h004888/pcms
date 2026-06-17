package com.pcms.userservice.dto.response;

import java.time.LocalDateTime;

/**
 * Response for forgot-password. resetToken is returned for local/dev
 * integration
 * until notification-service email delivery is wired end-to-end.
 */
public record PasswordResetResponse(
        String message,
        String resetToken,
        LocalDateTime expiresAt) {
}