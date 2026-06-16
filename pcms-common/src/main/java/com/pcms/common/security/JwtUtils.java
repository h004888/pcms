package com.pcms.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Standalone JWT utility for PCMS services (CR-02).
 *
 * <p>Reuses the {@link JwtClaims} contract for claim names so all services
 * stay consistent. Can be used by:
 * <ul>
 *   <li>{@code user-service} to <b>issue</b> tokens on login.</li>
 *   <li>Any service that needs to <b>validate</b> an incoming token locally
 *       (e.g., gateway filter or service-to-service auth).</li>
 * </ul>
 *
 * <p>This is intentionally simple — PCMS uses HS256 (per {@code STANDARDS.md} §12.2).
 * For production-grade asymmetric signing, swap to RS256 + JWKS.
 *
 * <p>Usage:
 * <pre>{@code
 * // Issue
 * String token = JwtUtils.generateAccessToken(user, "secret-key-32-bytes", 900_000L);
 *
 * // Validate
 * Claims claims = JwtUtils.parseAndValidate(token, "secret-key-32-bytes");
 * String role = claims.get(JwtClaims.ROLE, String.class);
 * }</pre>
 */
public final class JwtUtils {

    private JwtUtils() {
        // utility
    }

    /**
     * Build a SecretKey from the configured secret string.
     * Secret must be at least 32 bytes for HS256.
     */
    public static SecretKey signingKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate an access/refresh token with the PCMS claim contract.
     */
    public static String generateToken(UUID userId, String email, String role, UUID branchId,
                                      String type, long expirationMs, String secret) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaims.EMAIL, email);
        claims.put(JwtClaims.ROLE, role);
        claims.put(JwtClaims.BRANCH_ID, branchId != null ? branchId.toString() : null);
        claims.put(JwtClaims.TYPE, type);

        return Jwts.builder()
                .claims(claims)
                .subject(userId.toString())
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey(secret), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Parse and validate a JWT token. Throws {@link JwtException} on failure.
     */
    public static Claims parseAndValidate(String token, String secret) {
        return Jwts.parser()
                .verifyWith(signingKey(secret))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extract bearer token from raw {@code Authorization} header value.
     * Returns null if header is missing or malformed.
     */
    public static String extractBearerToken(String authHeader) {
        return JwtClaims.extractToken(authHeader);
    }
}
