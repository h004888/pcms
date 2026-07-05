package com.pcms.userservice.controller;

import com.pcms.common.security.JwtClaims;
import com.pcms.userservice.dto.request.ChangePasswordRequest;
import com.pcms.userservice.dto.request.ForgotPasswordRequest;
import com.pcms.userservice.dto.request.LoginRequest;
import com.pcms.userservice.dto.request.RegisterRequest;
import com.pcms.userservice.dto.request.ResendVerificationRequest;
import com.pcms.userservice.dto.request.ResetPasswordRequest;
import com.pcms.userservice.dto.request.VerifyEmailRequest;
import com.pcms.userservice.dto.response.CurrentUserResponse;
import com.pcms.userservice.dto.response.LoginResponse;
import com.pcms.userservice.dto.response.MessageResponse;
import com.pcms.userservice.dto.response.PasswordResetResponse;
import com.pcms.userservice.dto.response.ResendVerificationResponse;
import com.pcms.userservice.service.EmailVerificationService;
import com.pcms.userservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * UC01 - Login (FR1.1, FR1.2, FR1.3, BR05)
 * Thin controller: delegates business logic to UserService.
 * All business exceptions (InvalidCredentials, AccountLocked, InactiveAccount)
 * are translated to HTTP responses by pcms-common's GlobalExceptionHandler.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final EmailVerificationService emailVerificationService;

    public AuthController(UserService userService,
            EmailVerificationService emailVerificationService) {
        this.userService = userService;
        this.emailVerificationService = emailVerificationService;
    }

    /** POST /api/v1/auth/login - Step 1-8 of UC01 main flow */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(userService.login(request, httpRequest.getRemoteAddr()));
    }

    /** POST /api/v1/auth/register - Sprint 4: Customer self-registration */
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.register(request, httpRequest.getRemoteAddr()));
    }

    /** POST /api/v1/auth/forgot-password - UC01 forgot password */
    @PostMapping("/forgot-password")
    public ResponseEntity<PasswordResetResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(userService.forgotPassword(request, httpRequest.getRemoteAddr()));
    }

    /** POST /api/v1/auth/reset-password - UC01 reset password */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request,
            HttpServletRequest httpRequest) {
        userService.resetPassword(request, httpRequest.getRemoteAddr());
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    /** POST /api/v1/auth/refresh - Refresh access token */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(userService.refresh(request.get("refreshToken")));
    }

    /** POST /api/v1/auth/logout - NSF-07 token blacklist */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = JwtClaims.AUTH_HEADER, required = false) String authHeader,
            @RequestBody(required = false) Map<String, String> request) {
        String accessToken = JwtClaims.extractToken(authHeader);
        String refreshToken = request != null ? request.get("refreshToken") : null;
        userService.logout(accessToken, refreshToken);
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    // ====================================================================
    // Sprint 1 - new auth endpoints
    // ====================================================================

    /**
     * GET /api/v1/auth/me - TICKET-101.
     * Returns the currently authenticated user's profile + permission list.
     * The user id is propagated by the API Gateway via {@code X-User-Id}
     * (extracted from the JWT subject).
     */
    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> me(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        java.util.UUID userId = parseUuid(userIdHeader);
        return ResponseEntity.ok(userService.me(userId));
    }

    /**
     * PUT /api/v1/auth/password - TICKET-102.
     * Change the currently authenticated user's password (FR1.3). On success
     * all active refresh tokens for this user are revoked (forces re-login
     * on other devices).
     */
    @PutMapping("/password")
    public ResponseEntity<MessageResponse> changePassword(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest) {
        java.util.UUID userId = parseUuid(userIdHeader);
        userService.changePassword(userId, request, httpRequest.getRemoteAddr());
        return ResponseEntity.ok(MessageResponse.of("Đổi mật khẩu thành công"));
    }

    /**
     * POST /api/v1/auth/verify-email - TICKET-103.
     * Confirms a verification token received via email. Idempotent: if the
     * user is already verified the call still returns 200.
     */
    @PostMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request,
            HttpServletRequest httpRequest) {
        emailVerificationService.verifyEmail(request.token(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok(MessageResponse.of("Email đã được xác thực thành công"));
    }

    /**
     * POST /api/v1/auth/resend-verification - TICKET-104.
     * Re-issues a fresh verification token for the given email. Always
     * returns a generic 200 response to prevent email enumeration.
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<ResendVerificationResponse> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request,
            HttpServletRequest httpRequest) {
        EmailVerificationService.EmailVerificationIssueResult result =
                emailVerificationService.resendVerification(request.email(), httpRequest.getRemoteAddr());
        if (!result.issued()) {
            return ResponseEntity.ok(ResendVerificationResponse.generic());
        }
        return ResponseEntity.ok(ResendVerificationResponse.issued(result.token(), result.expiresAt()));
    }

    private static java.util.UUID parseUuid(String value) {
        if (value == null || value.isBlank() || "null".equalsIgnoreCase(value)) {
            return null;
        }
        try {
            return java.util.UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
