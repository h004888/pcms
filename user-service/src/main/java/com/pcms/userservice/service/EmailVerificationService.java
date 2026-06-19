package com.pcms.userservice.service;

import com.pcms.common.exception.EmailNotVerifiedException;
import com.pcms.common.exception.InvalidOperationException;
import com.pcms.common.exception.ResourceNotFoundException;
import com.pcms.userservice.entity.AuditLog;
import com.pcms.userservice.entity.EmailVerificationToken;
import com.pcms.userservice.entity.User;
import com.pcms.userservice.repository.AuditLogRepository;
import com.pcms.userservice.repository.EmailVerificationTokenRepository;
import com.pcms.userservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

/**
 * Email verification service (TICKET-103 + TICKET-104, FR1.6).
 *
 * <p>Handles two flows:
 * <ul>
 *   <li>{@link #verifyEmail(String, String)}: client posts the token received
 *       via email link; service marks the user as verified.</li>
 *   <li>{@link #resendVerification(String, String)}: client re-requests a
 *       verification email for the given address. Returns the token directly
 *       in dev (no email gateway wired in this codebase yet); in production
 *       the token would be passed to the notification-service.</li>
 * </ul>
 *
 * <p>Tokens are 24h TTL (per UC01 AT3). Only the SHA-256 hash is persisted
 * (CR-08). At most one active token is allowed per user - the resend flow
 * invalidates previous outstanding tokens before issuing a new one.
 */
@Service
public class EmailVerificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationService.class);
    private static final int TOKEN_TTL_HOURS = 24;

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final AuditLogRepository auditLogRepository;

    public EmailVerificationService(UserRepository userRepository,
                                    EmailVerificationTokenRepository tokenRepository,
                                    AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void verifyEmail(String rawToken, String ipAddress) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidOperationException(
                    "Verification token is required",
                    "Token xác thực không được để trống");
        }
        String tokenHash = sha256(rawToken);
        EmailVerificationToken token = tokenRepository.findFirstByTokenHashAndUsedAtIsNull(tokenHash)
                .orElseThrow(() -> new InvalidOperationException(
                        "Invalid or already-used verification token",
                        "Token xác thực không hợp lệ hoặc đã được sử dụng",
                        400));

        LocalDateTime now = LocalDateTime.now();
        if (token.isExpired(now)) {
            throw new EmailNotVerifiedException();
        }

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", token.getUserId()));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            // Idempotent: already verified, just mark token used and return.
            token.setUsedAt(now);
            tokenRepository.save(token);
            return;
        }

        user.setEmailVerified(true);
        userRepository.save(user);
        token.setUsedAt(now);
        tokenRepository.save(token);

        auditLogRepository.save(new AuditLog(
                user.getId(), "EMAIL_VERIFIED", user.getId(), ipAddress,
                "Email verified successfully"));
    }

    /**
     * Issue a fresh verification token for the given email. To prevent
     * email-enumeration attacks the response is always
     * {@code EmailVerificationIssueResult.notFound()} when the user does
     * not exist or is already verified.
     */
    @Transactional
    public EmailVerificationIssueResult resendVerification(String email, String ipAddress) {
        if (email == null || email.isBlank()) {
            return EmailVerificationIssueResult.notFound();
        }
        Optional<User> opt = userRepository.findByEmail(email.trim().toLowerCase());
        if (opt.isEmpty() || Boolean.TRUE.equals(opt.get().getEmailVerified())) {
            return EmailVerificationIssueResult.notFound();
        }
        User user = opt.get();

        // Invalidate all active tokens for the user
        tokenRepository.deleteAllActiveByUserId(user.getId());

        String token = UUID.randomUUID().toString() + "." + UUID.randomUUID();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(TOKEN_TTL_HOURS);
        tokenRepository.save(new EmailVerificationToken(
                user.getId(), sha256(token), expiresAt));

        auditLogRepository.save(new AuditLog(
                user.getId(), "EMAIL_VERIFICATION_RESENT", user.getId(), ipAddress,
                "Email verification token re-issued"));

        // Dev convenience: in production, route through notification-service
        log.info("[EMAIL] Would send verification link to: {} token={}", user.getEmail(), token);

        return new EmailVerificationIssueResult(true, token, expiresAt);
    }

    private String sha256(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    /** Outcome of {@link #resendVerification} - kept tiny to avoid coupling controllers to entities. */
    public record EmailVerificationIssueResult(boolean issued, String token, LocalDateTime expiresAt) {
        public static EmailVerificationIssueResult notFound() {
            return new EmailVerificationIssueResult(false, null, null);
        }
    }
}
