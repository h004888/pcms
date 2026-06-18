package com.pcms.userservice.service;

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
}
