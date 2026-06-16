package com.pcms.userservice.security;

import com.pcms.common.security.JwtClaims;
import com.pcms.common.security.JwtUtils;
import com.pcms.userservice.entity.User;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * UC01 - JWT Token generation/validation (FR1.2).
 *
 * <p>Issues and validates JWT tokens using the standard PCMS claim contract
 * (see {@link JwtClaims}). Backed by {@link JwtUtils} for the actual
 * signing/parsing logic.
 *
 * <ul>
 *   <li>Access token: 15 minutes</li>
 *   <li>Refresh token: 7 days</li>
 *   <li>Algorithm: HS256 (per STANDARDS.md §12.2)</li>
 * </ul>
 *
 * <p>The signing secret is loaded from {@code app.jwt.secret} via Config Server.
 */
@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    public String generateAccessToken(User user) {
        return JwtUtils.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getBranchId(),
                JwtClaims.TYPE_ACCESS,
                accessTokenExpirationMs,
                jwtSecret
        );
    }

    public String generateRefreshToken(User user) {
        return JwtUtils.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getBranchId(),
                JwtClaims.TYPE_REFRESH,
                refreshTokenExpirationMs,
                jwtSecret
        );
    }

    /**
     * Validate token signature + expiration. Returns parsed claims.
     */
    public Claims parseAndValidate(String token) {
        return JwtUtils.parseAndValidate(token, jwtSecret);
    }

    /**
     * Extract user UUID from a valid token.
     */
    public UUID extractUserId(String token) {
        Claims claims = parseAndValidate(token);
        return JwtClaims.parseUuidOrNull(claims.getSubject());
    }
}
