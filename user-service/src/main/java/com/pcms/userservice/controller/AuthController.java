package com.pcms.userservice.controller;

import com.pcms.userservice.dto.request.LoginRequest;
import com.pcms.userservice.dto.response.LoginResponse;
import com.pcms.userservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    /** POST /api/v1/auth/refresh - Refresh access token */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> request) {
        // TODO: validate refresh token, generate new access token
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(Map.of("message", "Not implemented yet"));
    }

    /** POST /api/v1/auth/logout - NSF-07 token blacklist */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // TODO: push revoked token to gateway blacklist
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }
}
