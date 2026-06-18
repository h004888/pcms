package com.pcms.userservice.controller;

import com.pcms.common.security.JwtClaims;
import com.pcms.userservice.dto.request.ForgotPasswordRequest;
import com.pcms.userservice.dto.request.LoginRequest;
import com.pcms.userservice.dto.request.ResetPasswordRequest;
import com.pcms.userservice.dto.response.LoginResponse;
import com.pcms.userservice.dto.response.PasswordResetResponse;
import com.pcms.userservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
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

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /** POST /api/v1/auth/login - Step 1-8 of UC01 main flow */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(userService.login(request, httpRequest.getRemoteAddr()));
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
}
