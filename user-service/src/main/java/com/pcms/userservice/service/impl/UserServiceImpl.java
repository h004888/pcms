package com.pcms.userservice.service.impl;

import com.pcms.common.exception.AccountLockedException;
import com.pcms.common.exception.DuplicateResourceException;
import com.pcms.common.exception.InactiveAccountException;
import com.pcms.common.exception.InvalidCredentialsException;
import com.pcms.common.exception.InvalidOperationException;
import com.pcms.common.exception.ResourceNotFoundException;
import com.pcms.common.security.JwtClaims;
import com.pcms.userservice.dto.request.CreateUserRequest;
import com.pcms.userservice.dto.request.ForgotPasswordRequest;
import com.pcms.userservice.dto.request.LoginRequest;
import com.pcms.userservice.dto.request.ResetPasswordRequest;
import com.pcms.userservice.dto.request.UpdateUserRequest;
import com.pcms.userservice.dto.response.AuditLogResponse;
import com.pcms.userservice.dto.response.DashboardStatsResponse;
import com.pcms.userservice.dto.response.LoginResponse;
import com.pcms.userservice.dto.response.PasswordResetResponse;
import com.pcms.userservice.dto.response.UserResponse;
import com.pcms.userservice.entity.AuditLog;
import com.pcms.userservice.entity.PasswordResetToken;
import com.pcms.userservice.entity.RefreshToken;
import com.pcms.userservice.entity.TokenBlacklist;
import com.pcms.userservice.entity.User;
import com.pcms.userservice.repository.RefreshTokenRepository;
import com.pcms.userservice.repository.TokenBlacklistRepository;
import com.pcms.userservice.repository.AuditLogRepository;
import com.pcms.userservice.repository.PasswordResetTokenRepository;
import com.pcms.userservice.repository.UserRepository;
import com.pcms.userservice.enums.Role;
import com.pcms.userservice.enums.UserStatus;
import com.pcms.userservice.security.JwtService;
import com.pcms.userservice.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final long ACCESS_TOKEN_EXPIRES_IN_SECONDS = 900L; // 15 minutes
    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;
    private static final int PASSWORD_RESET_TOKEN_MINUTES = 15;
    private static final int EXPORT_MAX_ROWS = 10_000;
    private static final String PASSWORD_RESET_GENERIC_MESSAGE = "Nếu email tồn tại, "
            + "hệ thống đã tạo hướng dẫn đặt lại mật khẩu";

    private final UserRepository repository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserServiceImpl(UserRepository repository,
            RefreshTokenRepository refreshTokenRepository,
            TokenBlacklistRepository tokenBlacklistRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            AuditLogRepository auditLogRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.repository = repository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenBlacklistRepository = tokenBlacklistRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> list(String search, Role role, UUID branchId, UserStatus status, Pageable pageable) {
        return repository.searchUsers(search, role, branchId, status, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return toResponse(user);
    }

    @Override
    public UserResponse create(CreateUserRequest request, String rawPassword) {
        if (repository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email", request.email());
        }
        User user = new User();
        user.setEmail(request.email());
        user.setFullName(request.fullName());
        user.setPhone(request.phone());
        user.setRole(request.role());
        user.setBranchId(request.branchId());
        user.setStatus(UserStatus.ACTIVE);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setFailedLoginCount(0);
        User saved = repository.save(user);
        audit("USER_CREATED", saved.getId(), saved.getId(), null, "Created user " + saved.getEmail());
        return toResponse(saved);
    }

    @Override
    public UserResponse update(UUID id, UpdateUserRequest request) {
        User user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setFullName(request.fullName());
        user.setPhone(request.phone());
        user.setRole(request.role());
        user.setBranchId(request.branchId());
        user.setStatus(request.status());
        User saved = repository.save(user);
        audit("USER_UPDATED", saved.getId(), saved.getId(), null, "Updated user " + saved.getEmail());
        return toResponse(saved);
    }

    @Override
    public void softDelete(UUID id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setStatus(UserStatus.INACTIVE);
        repository.save(user);
        audit("USER_DEACTIVATED", id, id, null, "Deactivated user " + user.getEmail());
    }

    @Override
    public UserResponse changeRole(UUID id, Role role) {
        Objects.requireNonNull(role, "role must not be null");
        User user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setRole(role);
        User saved = repository.save(user);
        audit("USER_ROLE_CHANGED", saved.getId(), saved.getId(), null, "Changed role to " + role);
        return toResponse(saved);
    }

    @Override
    public UserResponse changeStatus(UUID id, UserStatus status) {
        Objects.requireNonNull(status, "status must not be null");
        User user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setStatus(status);
        if (status != UserStatus.LOCKED) {
            user.setLockedUntil(null);
        } else if (user.getLockedUntil() == null) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES));
        }
        User saved = repository.save(user);
        audit("USER_STATUS_CHANGED", saved.getId(), saved.getId(), null, "Changed status to " + status);
        return toResponse(saved);
    }

    @Override
    public UserResponse unlock(UUID id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setStatus(UserStatus.ACTIVE);
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        User saved = repository.save(user);
        audit("USER_UNLOCKED", saved.getId(), saved.getId(), null, "Unlocked user " + saved.getEmail());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public String exportCsv(String search, Role role, UUID branchId, UserStatus status) {
        Pageable pageable = PageRequest.of(0, EXPORT_MAX_ROWS, Sort.by("createdAt").descending());
        Page<UserResponse> users = list(search, role, branchId, status, pageable);
        StringBuilder csv = new StringBuilder("id,email,fullName,phone,role,branchId,status,lastLoginAt,createdAt\n");
        users.forEach(user -> csv.append(user.id()).append(',')
                .append(escapeCsv(user.email())).append(',')
                .append(escapeCsv(user.fullName())).append(',')
                .append(escapeCsv(user.phone())).append(',')
                .append(user.role()).append(',')
                .append(user.branchId() == null ? "" : user.branchId()).append(',')
                .append(user.status()).append(',')
                .append(user.lastLoginAt() == null ? "" : user.lastLoginAt()).append(',')
                .append(user.createdAt() == null ? "" : user.createdAt()).append('\n'));
        return csv.toString();
    }

    @Override
    public LoginResponse login(LoginRequest request, String ipAddress) {
        // Security: don't reveal whether the email exists
        User user = repository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        // BR05: Check account lockout
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new AccountLockedException(user.getLockedUntil());
        }

        // Step 4: Verify password
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            int failedCount = (user.getFailedLoginCount() == null ? 0 : user.getFailedLoginCount()) + 1;
            user.setFailedLoginCount(failedCount);
            LocalDateTime now = LocalDateTime.now();
            if (failedCount >= MAX_FAILED_LOGIN_ATTEMPTS) {
                user.setLockedUntil(now.plusMinutes(LOCKOUT_DURATION_MINUTES));
                user.setStatus(UserStatus.LOCKED);
            }
            repository.save(user);
            // Throw AFTER persisting the lock so the lock is recorded
            if (failedCount >= MAX_FAILED_LOGIN_ATTEMPTS) {
                throw new AccountLockedException(user.getLockedUntil());
            }
            throw new InvalidCredentialsException();
        }

        // Check status
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new InactiveAccountException();
        }

        // Step 5-7: Generate tokens, update login info
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        persistRefreshToken(user, refreshToken);

        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(ipAddress);
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        if (user.getStatus() == UserStatus.LOCKED) {
            user.setStatus(UserStatus.ACTIVE);
        }
        repository.save(user);
        audit("LOGIN_SUCCESS", user.getId(), user.getId(), ipAddress, "User logged in");

        return new LoginResponse(
                accessToken,
                refreshToken,
                "Bearer",
                ACCESS_TOKEN_EXPIRES_IN_SECONDS,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getBranchId());
    }

    @Override
    public LoginResponse refresh(String refreshToken) {
        Claims claims = parseTokenOrThrow(refreshToken, "Refresh token không hợp lệ hoặc đã hết hạn");
        if (!jwtService.isRefreshToken(claims)) {
            throw new InvalidOperationException(
                    "Token is not a refresh token",
                    "Token không phải refresh token",
                    401);
        }

        String jti = jwtService.extractJti(claims);
        RefreshToken stored = refreshTokenRepository.findByJti(jti)
                .orElseThrow(() -> new InvalidOperationException(
                        "Refresh token has been revoked",
                        "Refresh token đã bị thu hồi",
                        401));

        LocalDateTime now = LocalDateTime.now();
        if (stored.isRevoked() || stored.getExpiresAt().isBefore(now)) {
            throw new InvalidOperationException(
                    "Refresh token is no longer active",
                    "Refresh token không còn hiệu lực",
                    401);
        }
        if (!stored.getTokenHash().equals(sha256(refreshToken))) {
            throw new InvalidOperationException("Refresh token mismatch", "Refresh token không hợp lệ", 401);
        }

        User user = repository.findById(stored.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", stored.getUserId()));
        ensureUserCanReceiveToken(user);

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);
        Claims newRefreshClaims = jwtService.parseAndValidate(newRefreshToken);

        stored.setRevoked(true);
        stored.setRevokedAt(now);
        stored.setReplacedByJti(jwtService.extractJti(newRefreshClaims));
        refreshTokenRepository.save(stored);
        persistRefreshToken(user, newRefreshToken, newRefreshClaims);

        return new LoginResponse(
                newAccessToken,
                newRefreshToken,
                "Bearer",
                ACCESS_TOKEN_EXPIRES_IN_SECONDS,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getBranchId());
    }

    @Override
    public void logout(String accessToken, String refreshToken) {
        UUID userId = null;
        if (accessToken != null && !accessToken.isBlank()) {
            userId = extractUserIdIfValid(accessToken);
            blacklistTokenIfValid(accessToken, "LOGOUT");
        }
        if (refreshToken != null && !refreshToken.isBlank()) {
            Claims claims = parseTokenOrThrow(refreshToken, "Refresh token không hợp lệ hoặc đã hết hạn");
            if (!jwtService.isRefreshToken(claims)) {
                throw new InvalidOperationException("Token is not a refresh token", "Token không phải refresh token",
                        401);
            }
            refreshTokenRepository.findByJti(jwtService.extractJti(claims)).ifPresent(token -> {
                token.setRevoked(true);
                token.setRevokedAt(LocalDateTime.now());
                refreshTokenRepository.save(token);
            });
            blacklistClaims(claims, "LOGOUT");
            if (userId == null) {
                userId = JwtClaims.parseUuidOrNull(claims.getSubject());
            }
        }
        audit("LOGOUT", userId, userId, null, "User logged out");
    }

    @Override
    public PasswordResetResponse forgotPassword(ForgotPasswordRequest request, String ipAddress) {
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(PASSWORD_RESET_TOKEN_MINUTES);
        return repository.findByEmail(request.email())
                .map(user -> createPasswordResetToken(user, expiresAt, ipAddress, PASSWORD_RESET_GENERIC_MESSAGE))
                .orElseGet(() -> new PasswordResetResponse(PASSWORD_RESET_GENERIC_MESSAGE, null, expiresAt));
    }

    @Override
    public void resetPassword(ResetPasswordRequest request, String ipAddress) {
        String token = request.token();
        String jti = extractResetJti(token);
        PasswordResetToken stored = passwordResetTokenRepository.findByJti(jti)
                .orElseThrow(() -> new InvalidOperationException(
                        "Invalid password reset token",
                        "Token đặt lại mật khẩu không hợp lệ"));
        LocalDateTime now = LocalDateTime.now();
        if (stored.getUsedAt() != null || stored.getExpiresAt().isBefore(now)) {
            throw new InvalidOperationException(
                    "Password reset token is expired or already used",
                    "Token đặt lại mật khẩu đã hết hạn hoặc đã được sử dụng");
        }
        if (!stored.getTokenHash().equals(sha256(token))) {
            throw new InvalidOperationException(
                    "Invalid password reset token",
                    "Token đặt lại mật khẩu không hợp lệ");
        }
        User user = repository.findById(stored.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", stored.getUserId()));
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        if (user.getStatus() == UserStatus.LOCKED) {
            user.setStatus(UserStatus.ACTIVE);
        }
        stored.setUsedAt(now);
        repository.save(user);
        passwordResetTokenRepository.save(stored);
        audit("PASSWORD_RESET", user.getId(), user.getId(), ipAddress, "Password reset completed");
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse dashboardStats() {
        return new DashboardStatsResponse(
                repository.count(),
                repository.countByStatus(UserStatus.ACTIVE),
                repository.countByStatus(UserStatus.LOCKED),
                repository.countByStatus(UserStatus.INACTIVE),
                repository.countByRole(Role.BRANCH_MANAGER),
                repository.countByRole(Role.PHARMACIST),
                repository.countByRole(Role.ADMIN) + repository.countByRole(Role.CEO));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> recentLogins(Pageable pageable) {
        return repository.findByLastLoginAtIsNotNullOrderByLastLoginAtDesc(pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> auditLogs(UUID userId, String action, Pageable pageable) {
        return auditLogRepository.search(userId, action, pageable).map(AuditLogResponse::from);
    }

    private void ensureUserCanReceiveToken(User user) {
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new InactiveAccountException();
        }
        if (user.getStatus() == UserStatus.LOCKED
                || (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now()))) {
            throw new AccountLockedException(user.getLockedUntil());
        }
    }

    private void persistRefreshToken(User user, String refreshToken) {
        persistRefreshToken(user, refreshToken, jwtService.parseAndValidate(refreshToken));
    }

    private void persistRefreshToken(User user, String refreshToken, Claims claims) {
        refreshTokenRepository.save(new RefreshToken(
                user.getId(),
                jwtService.extractJti(claims),
                sha256(refreshToken),
                jwtService.extractExpiration(claims)));
    }

    private Claims parseTokenOrThrow(String token, String messageVi) {
        try {
            return jwtService.parseAndValidate(token);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new InvalidOperationException("Invalid or expired token", messageVi, 401);
        }
    }

    private void blacklistTokenIfValid(String token, String reason) {
        try {
            blacklistClaims(jwtService.parseAndValidate(token), reason);
        } catch (JwtException | IllegalArgumentException ignored) {
            // Logout should be idempotent: an already-expired access token does not need
            // blacklisting.
        }
    }

    private void blacklistClaims(Claims claims, String reason) {
        String jti = jwtService.extractJti(claims);
        if (jti == null || tokenBlacklistRepository.existsByJti(jti)) {
            return;
        }
        tokenBlacklistRepository.save(new TokenBlacklist(
                jti,
                JwtClaims.parseUuidOrNull(claims.getSubject()),
                claims.get(JwtClaims.TYPE, String.class),
                jwtService.extractExpiration(claims),
                reason));
    }

    private PasswordResetResponse createPasswordResetToken(User user,
            LocalDateTime expiresAt,
            String ipAddress,
            String genericMessage) {
        String jti = UUID.randomUUID().toString();
        String token = jti + "." + UUID.randomUUID();
        passwordResetTokenRepository.save(new PasswordResetToken(
                user.getId(),
                jti,
                sha256(token),
                expiresAt));
        audit("PASSWORD_RESET_REQUESTED", user.getId(), user.getId(), ipAddress, "Password reset requested");
        return new PasswordResetResponse(genericMessage, token, expiresAt);
    }

    private String extractResetJti(String token) {
        int separatorIndex = token == null ? -1 : token.indexOf('.');
        if (separatorIndex <= 0) {
            throw new InvalidOperationException(
                    "Invalid password reset token",
                    "Token đặt lại mật khẩu không hợp lệ");
        }
        return token.substring(0, separatorIndex);
    }

    private UUID extractUserIdIfValid(String token) {
        try {
            return JwtClaims.parseUuidOrNull(jwtService.parseAndValidate(token).getSubject());
        } catch (JwtException | IllegalArgumentException ignored) {
            return null;
        }
    }

    private void audit(String action, UUID userId, UUID targetId, String ipAddress, String description) {
        auditLogRepository.save(new AuditLog(userId, action, targetId, ipAddress, description));
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\r")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private String sha256(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getRole(),
                user.getBranchId(),
                user.getStatus(),
                user.getLastLoginAt(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
