package com.pcms.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for {@code POST /api/v1/auth/verify-email} (TICKET-103).
 *
 * <p>The client receives the token via a verification email link; the link's
 * token is then POSTed here for confirmation. The token is opaque to the
 * client and is hashed server-side before lookup.
 */
public record VerifyEmailRequest(
        @NotBlank(message = "Token xác thực không được để trống")
        @Size(max = 200, message = "Token không hợp lệ")
        String token
) {
}
