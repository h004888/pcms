package com.pcms.userservice.service;

import com.pcms.userservice.dto.request.AssignBranchRequest;
import com.pcms.userservice.dto.request.ChangePasswordRequest;
import com.pcms.userservice.dto.request.CreateUserRequest;
import com.pcms.userservice.dto.request.ForgotPasswordRequest;
import com.pcms.userservice.dto.request.LoginRequest;
import com.pcms.userservice.dto.request.RegisterRequest;
import com.pcms.userservice.dto.request.ResendVerificationRequest;
import com.pcms.userservice.dto.request.ResetPasswordRequest;
import com.pcms.userservice.dto.request.UpdateUserRequest;
import com.pcms.userservice.dto.request.VerifyEmailRequest;
import com.pcms.userservice.dto.response.AuditLogResponse;
import com.pcms.userservice.dto.response.CurrentUserResponse;
import com.pcms.userservice.dto.response.DashboardStatsResponse;
import com.pcms.userservice.dto.response.LoginResponse;
import com.pcms.userservice.dto.response.PasswordResetResponse;
import com.pcms.userservice.dto.response.UserResponse;
import com.pcms.userservice.enums.Role;
import com.pcms.userservice.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface UserService {
    Page<UserResponse> list(String search, Role role, UUID branchId, UserStatus status, Pageable pageable);

    UserResponse getById(UUID id);

    UserResponse create(CreateUserRequest request, String rawPassword);

    UserResponse update(UUID id, UpdateUserRequest request);

    void softDelete(UUID id);

    UserResponse changeRole(UUID id, Role role);

    UserResponse changeStatus(UUID id, UserStatus status);

    UserResponse unlock(UUID id);

    String exportCsv(String search, Role role, UUID branchId, UserStatus status);

    LoginResponse login(LoginRequest request, String ipAddress);

    LoginResponse refresh(String refreshToken);

    void logout(String accessToken, String refreshToken);

    PasswordResetResponse forgotPassword(ForgotPasswordRequest request, String ipAddress);

    void resetPassword(ResetPasswordRequest request, String ipAddress);

    DashboardStatsResponse dashboardStats();

    Page<UserResponse> recentLogins(Pageable pageable);

    Page<AuditLogResponse> auditLogs(UUID userId, String action, Pageable pageable);

    // ===== Sprint 1 - new auth/user APIs =====

    /** TICKET-101: Return current user profile + permissions (FR1.4, SCR-HOME). */
    CurrentUserResponse me(UUID userId);

    /** TICKET-102: Change the currently authenticated user's password (FR1.3, BR05). */
    void changePassword(UUID userId, ChangePasswordRequest request, String ipAddress);

    /** TICKET-106: Assign a user to a branch (FR2.3). */
    UserResponse assignBranch(UUID userId, AssignBranchRequest request, String actorId);

    // Email verification (TICKET-103, TICKET-104) - delegate to EmailVerificationService
    void verifyEmail(VerifyEmailRequest request, String ipAddress);

    void resendVerification(ResendVerificationRequest request, String ipAddress);

    /** Sprint 4: Customer self-registration from portal. Creates user account + returns tokens. */
    LoginResponse register(RegisterRequest request, String ipAddress);
}
