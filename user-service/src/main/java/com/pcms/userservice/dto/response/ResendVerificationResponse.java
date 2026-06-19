package com.pcms.userservice.dto.response;

import java.time.LocalDateTime;

/**
 * Response payload for {@code POST /api/v1/auth/resend-verification}
 * (TICKET-104).
 *
 * <p>In dev mode the {@code token} field is populated so the tester can
 * paste the value into the {@code /auth/verify-email} request without
 * having an email gateway. In production the token would only be sent
 * to the user's mailbox and this field would be {@code null}.
 */
public record ResendVerificationResponse(
        String message,
        String token,
        LocalDateTime expiresAt
) {
    public static ResendVerificationResponse generic() {
        return new ResendVerificationResponse(
                "Nếu email tồn tại và chưa xác thực, hệ thống đã gửi lại hướng dẫn xác thực",
                null,
                null);
    }

    public static ResendVerificationResponse issued(String token, LocalDateTime expiresAt) {
        return new ResendVerificationResponse(
                "Đã gửi lại email xác thực",
                token,
                expiresAt);
    }
}
