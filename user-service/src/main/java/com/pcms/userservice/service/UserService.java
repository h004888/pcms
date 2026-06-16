package com.pcms.userservice.service;

import com.pcms.userservice.dto.request.CreateUserRequest;
import com.pcms.userservice.dto.request.LoginRequest;
import com.pcms.userservice.dto.request.UpdateUserRequest;
import com.pcms.userservice.dto.response.LoginResponse;
import com.pcms.userservice.dto.response.UserResponse;
import com.pcms.userservice.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface UserService {
    Page<UserResponse> list(String search, Role role, UUID branchId, Pageable pageable);
    UserResponse getById(UUID id);
    UserResponse create(CreateUserRequest request, String rawPassword);
    UserResponse update(UUID id, UpdateUserRequest request);
    void softDelete(UUID id);
    LoginResponse login(LoginRequest request, String ipAddress);
}
