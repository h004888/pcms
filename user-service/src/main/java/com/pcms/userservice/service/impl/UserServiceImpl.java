package com.pcms.userservice.service.impl;

import com.pcms.common.exception.AccountLockedException;
import com.pcms.common.exception.DuplicateResourceException;
import com.pcms.common.exception.InactiveAccountException;
import com.pcms.common.exception.InvalidCredentialsException;
import com.pcms.common.exception.ResourceNotFoundException;
import com.pcms.userservice.dto.request.CreateUserRequest;
import com.pcms.userservice.dto.request.LoginRequest;
import com.pcms.userservice.dto.request.UpdateUserRequest;
import com.pcms.userservice.dto.response.LoginResponse;
import com.pcms.userservice.dto.response.UserResponse;
import com.pcms.userservice.entity.User;
import com.pcms.userservice.enums.Role;
import com.pcms.userservice.enums.UserStatus;
import com.pcms.userservice.repository.UserRepository;
import com.pcms.userservice.security.JwtService;
import com.pcms.userservice.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final long ACCESS_TOKEN_EXPIRES_IN_SECONDS = 900L; // 15 minutes
    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserServiceImpl(UserRepository repository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> list(String search, Role role, UUID branchId, Pageable pageable) {
        return repository.searchUsers(search, role, branchId, pageable)
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
        return toResponse(saved);
    }

    @Override
    public void softDelete(UUID id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setStatus(UserStatus.INACTIVE);
        repository.save(user);
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

        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(ipAddress);
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        if (user.getStatus() == UserStatus.LOCKED) {
            user.setStatus(UserStatus.ACTIVE);
        }
        repository.save(user);

        return new LoginResponse(
                accessToken,
                refreshToken,
                "Bearer",
                ACCESS_TOKEN_EXPIRES_IN_SECONDS,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getBranchId()
        );
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
                user.getUpdatedAt()
        );
    }
}
