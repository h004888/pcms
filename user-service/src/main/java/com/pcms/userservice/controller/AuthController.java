package com.pcms.userservice.controller;

import com.pcms.userservice.entity.User;
import com.pcms.userservice.enums.UserStatus;
import com.pcms.userservice.repository.UserRepository;
import com.pcms.userservice.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * UC01 - Login (FR1.1, FR1.2, FR1.3, BR05)
 * Implements SRS §3.2.1 main flow + alternative flows
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /** POST /api/v1/auth/login - Step 1-8 of UC01 main flow */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        // Step 2: Validate input format
        if (request.email() == null || request.password() == null) {
            return ResponseEntity.badRequest().body(Map.of("code", "MSG33", "message", "Email and password required"));
        }

        // Step 3: Look up user
        Optional<User> optional = userRepository.findByEmail(request.email());
        if (optional.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("code", "MSG01", "message", "Invalid email or password"));
        }

        User user = optional.get();

        // Check account lockout (BR05)
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            return ResponseEntity.status(423).body(Map.of(
                "code", "MSG02",
                "message", "Account locked. Try again in 30 minutes",
                "lockedUntil", user.getLockedUntil()
            ));
        }

        // Step 4: Verify password
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            // AT2: Increment failed-attempt counter
            int failedCount = (user.getFailedLoginCount() == null ? 0 : user.getFailedLoginCount()) + 1;
            user.setFailedLoginCount(failedCount);
            if (failedCount >= 5) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(30));
                user.setStatus(UserStatus.LOCKED);
            }
            userRepository.save(user);
            return ResponseEntity.status(401).body(Map.of("code", "MSG01", "message", "Invalid email or password"));
        }

        // Check status
        if (user.getStatus() == UserStatus.INACTIVE) {
            return ResponseEntity.status(403).body(Map.of("code", "MSG31", "message", "Account is inactive"));
        }

        // Step 5-7: Generate tokens, update login info
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(httpRequest.getRemoteAddr());
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        if (user.getStatus() == UserStatus.LOCKED) user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        // Step 7: Return tokens and profile
        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("tokenType", "Bearer");
        response.put("expiresIn", 900);  // 15 min in seconds
        response.put("user", Map.of(
            "id", user.getId(),
            "email", user.getEmail(),
            "fullName", user.getFullName(),
            "role", user.getRole(),
            "branchId", user.getBranchId() != null ? user.getBranchId() : ""
        ));
        return ResponseEntity.ok(response);
    }

    /** POST /api/v1/auth/refresh - Refresh access token */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> request) {
        // TODO: validate refresh token, generate new access token
        return ResponseEntity.status(501).body(Map.of("message", "Not implemented yet"));
    }

    /** POST /api/v1/auth/logout - NSF-07 token blacklist */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // TODO: push revoked token to gateway blacklist
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    public record LoginRequest(String email, String password) {}
}
