package com.pcms.apigateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pcms.common.security.JwtClaims;
import com.pcms.common.security.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * API Gateway JWT authentication filter (B-01).
 *
 * <p>Validates Bearer token on every request EXCEPT for public endpoints
 * (auth flow + health probes + actuator). On success, propagates the
 * authenticated user identity as headers for downstream services:
 *
 * <ul>
 *   <li>{@code X-User-Id}    — UUID</li>
 *   <li>{@code X-User-Email} — email</li>
 *   <li>{@code X-User-Role}  — ADMIN / CEO / BRANCH_MANAGER / PHARMACIST / CUSTOMER</li>
 *   <li>{@code X-Branch-Id}  — UUID or null</li>
 * </ul>
 *
 * <p>Spring Cloud Gateway 5.x with server-webmvc uses servlet stack
 * (OncePerRequestFilter), not reactive GlobalFilter. This filter runs
 * BEFORE the gateway routing so it can reject unauthenticated requests
 * with 401 before they reach downstream services.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    /** Path prefixes that bypass JWT validation.
     *  Each prefix has 2 variants: with `/api/v1` (incoming) and without (post-rewrite). */
    private static final Set<String> PUBLIC_PREFIXES = Set.of(
            "/auth/login",          "/api/v1/auth/login",
            "/auth/forgot-password", "/api/v1/auth/forgot-password",
            "/auth/reset-password", "/api/v1/auth/reset-password",
            "/auth/verify-email",   "/api/v1/auth/verify-email",
            "/auth/resend-verification", "/api/v1/auth/resend-verification",
            "/auth/healthz",        "/api/v1/auth/healthz",
            "/auth/readyz",         "/api/v1/auth/readyz",
            "/auth/register",       "/api/v1/auth/register",
            "/webhooks/payment-gateway", "/api/v1/webhooks/payment-gateway",
            "/actuator",
            "/healthz",
            "/readyz"
    );

    private final String secret;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtAuthenticationFilter(@Value("${app.jwt.secret:}") String secret) {
        this.secret = secret;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // CORS preflight requests bypass JWT validation (browser sends OPTIONS without
        // Authorization header; Spring Cloud Gateway needs to respond with CORS headers
        // before the auth filter would otherwise reject with 401).
        if ("OPTIONS".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (isPublic(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader(JwtClaims.AUTH_HEADER);
        String token = JwtClaims.extractToken(authHeader);

        if (token == null) {
            unauthorized(response, path, "Missing or malformed Authorization header",
                    "Thiếu hoặc sai định dạng header Authorization");
            return;
        }

        Claims claims;
        try {
            claims = JwtUtils.parseAndValidate(token, secret);
        } catch (Exception e) {
            log.warn("[gateway] JWT validation failed for {} -> {}", path, e.getMessage());
            unauthorized(response, path, "Invalid or expired token",
                    "Token không hợp lệ hoặc đã hết hạn");
            return;
        }

        // Propagate identity as request attributes (downstream can read via @RequestHeader)
        request.setAttribute("X-User-Id", String.valueOf(claims.getSubject()));
        request.setAttribute("X-User-Email", claims.get(JwtClaims.EMAIL, String.class));
        request.setAttribute("X-User-Role", claims.get(JwtClaims.ROLE, String.class));
        String branchId = claims.get(JwtClaims.BRANCH_ID, String.class);
        if (branchId != null && !"null".equalsIgnoreCase(branchId)) {
            request.setAttribute("X-Branch-Id", branchId);
        }

        // Also set as headers for downstream services that read from HttpHeaders
        // (wrapped request to avoid modifying the original)
        var wrappedRequest = new AuthenticatedRequestWrapper(request, claims);
        filterChain.doFilter(wrappedRequest, response);
    }

    private boolean isPublic(String path) {
        if (path == null) return false;
        for (String prefix : PUBLIC_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private void unauthorized(HttpServletResponse response, String path,
                              String message, String messageVi) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", "MSG01");
        body.put("status", 401);
        body.put("message", message);
        body.put("messageVi", messageVi);
        body.put("path", path);
        body.put("timestamp", Instant.now().toString());

        try {
            objectMapper.writeValue(response.getOutputStream(), body);
        } catch (JsonProcessingException e) {
            response.getWriter().write("{\"code\":\"MSG01\",\"status\":401}");
        }
    }

    /**
     * HttpServletRequest wrapper that exposes authenticated user info as
     * request headers ({@code X-User-Id}, {@code X-User-Role}, etc.)
     * so downstream services can read via {@code @RequestHeader}.
     */
    private static class AuthenticatedRequestWrapper extends jakarta.servlet.http.HttpServletRequestWrapper {
        private final Map<String, String> authHeaders = new LinkedHashMap<>();

        AuthenticatedRequestWrapper(HttpServletRequest request, Claims claims) {
            super(request);
            Object subject = claims.getSubject();
            String email = claims.get(JwtClaims.EMAIL, String.class);
            String role = claims.get(JwtClaims.ROLE, String.class);
            String branchId = claims.get(JwtClaims.BRANCH_ID, String.class);
            if (subject != null) authHeaders.put("X-User-Id", String.valueOf(subject));
            if (email != null) authHeaders.put("X-User-Email", email);
            if (role != null) authHeaders.put("X-User-Role", role);
            if (branchId != null && !"null".equalsIgnoreCase(branchId)) {
                authHeaders.put("X-Branch-Id", branchId);
            }
        }

        @Override
        public String getHeader(String name) {
            String v = authHeaders.get(name);
            return v != null ? v : super.getHeader(name);
        }

        @Override
        public java.util.Enumeration<String> getHeaders(String name) {
            String override = authHeaders.get(name);
            if (override != null) {
                return java.util.Collections.enumeration(java.util.Collections.singletonList(override));
            }
            return super.getHeaders(name);
        }

        @Override
        public java.util.Enumeration<String> getHeaderNames() {
            java.util.Set<String> names = new java.util.LinkedHashSet<>(authHeaders.keySet());
            java.util.Enumeration<String> original = super.getHeaderNames();
            while (original.hasMoreElements()) {
                names.add(original.nextElement());
            }
            return java.util.Collections.enumeration(names);
        }
    }
}
