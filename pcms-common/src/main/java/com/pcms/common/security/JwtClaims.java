package com.pcms.common.security;

import java.util.UUID;

/**
 * Standard claims contract for PCMS JWT tokens (CR-02 / SRS UC01).
 *
 * <p>PCMS uses HS256-signed JWT (per {@code STANDARDS.md} §12.2) — simple and
 * sufficient for an internal microservice mesh where the API Gateway already
 * validates the token before forwarding to downstream services.
 *
 * <p>Token shape:
 * <pre>{@code
 * {
 *   "sub":      "uuid",          // user id
 *   "email":    "user@example.com",
 *   "role":     "PHARMACIST",
 *   "branchId": "uuid|null",
 *   "type":     "access|refresh",
 *   "iat":      1718000000,
 *   "exp":      1718000900,
 *   "jti":      "uuid"
 * }
 * }</pre>
 *
 * <p>Claim names use <b>camelCase</b> (not snake_case) to align with the
 * Spring Security {@code JwtAuthenticationConverter} convention. Earlier
 * versions used snake_case — this is a breaking change but enforced
 * consistently across the codebase as of v1.3.0.
 */
public final class JwtClaims {

    public static final String SUB = "sub";
    public static final String EMAIL = "email";
    public static final String ROLE = "role";
    public static final String BRANCH_ID = "branchId";
    public static final String TYPE = "type";
    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";
    public static final String JTI = "jti";
    public static final String IAT = "iat";
    public static final String EXP = "exp";

    /** HTTP header carrying the access token. */
    public static final String AUTH_HEADER = "Authorization";

    /** Token prefix per RFC 6750. */
    public static final String BEARER_PREFIX = "Bearer ";

    private JwtClaims() {
        // utility
    }

    /**
     * Helper to build a {@code Authorization} header value.
     */
    public static String bearer(String token) {
        return BEARER_PREFIX + token;
    }

    /**
     * Extract bearer token from raw {@code Authorization} header value.
     */
    public static String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return authHeader.substring(BEARER_PREFIX.length()).trim();
    }

    /**
     * Safe UUID parser for claim values.
     */
    public static UUID parseUuidOrNull(String value) {
        if (value == null || value.isBlank() || "null".equalsIgnoreCase(value)) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
