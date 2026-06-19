package com.pcms.userservice.dto.request;

import com.pcms.userservice.dto.request.ValidationMessages;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for {@code POST /api/v1/auth/resend-verification}
 * (TICKET-104).
 *
 * <p>To prevent email-enumeration, the response is always
 * {@code 200 OK} with a generic success message regardless of whether the
 * email exists. Rate limiting (1 request/60s/user) is enforced inside
 * the service layer.
 */
public record ResendVerificationRequest(
        @NotBlank(message = ValidationMessages.EMAIL_REQUIRED)
        String email
) {
}
