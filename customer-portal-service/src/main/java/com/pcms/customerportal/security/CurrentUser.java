package com.pcms.customerportal.security;

import com.pcms.common.security.JwtClaims;

import java.util.UUID;

/**
 * Helpers for resolving the current customer from request headers.
 * <p>The API Gateway forwards JWT identity via {@code X-User-Id} (subject
 * claim) - downstream services trust that header and treat it as the
 * authoritative customer id. For B2C, the user IS the customer.
 */
public final class CurrentUser {

    public static final String USER_ID_HEADER = "X-User-Id";

    private CurrentUser() {}

    /**
     * Resolve the current customer id from {@code X-User-Id} header.
     * Throws {@link MissingUserHeaderException} (mapped to 401) if absent.
     */
    public static UUID requireCustomerId(String headerValue) {
        UUID id = JwtClaims.parseUuidOrNull(headerValue);
        if (id == null) {
            throw new MissingUserHeaderException(
                    "X-User-Id header is required and must be a valid UUID",
                    "Thiếu header X-User-Id hoặc không hợp lệ");
        }
        return id;
    }

    /** Optional resolver - returns null when header is absent. */
    public static UUID optionalCustomerId(String headerValue) {
        return JwtClaims.parseUuidOrNull(headerValue);
    }
}
