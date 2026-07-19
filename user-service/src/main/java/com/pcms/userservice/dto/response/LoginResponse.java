package com.pcms.userservice.dto.response;

import com.pcms.userservice.entity.User;
import com.pcms.userservice.enums.Role;
import com.pcms.userservice.enums.UserStatus;
import java.util.UUID;

/**
 * Authentication response payload returned by {@code POST /auth/login} and
 * {@code POST /auth/refresh}.
 *
 * <p>The user profile is wrapped in a nested {@link UserInfo} record to keep the
 * token-related fields ({@code accessToken}, {@code refreshToken}, ...) at the
 * top level while still exposing the full user object — matching the contract
 * expected by the Next.js frontend (see docs/api-contract.md).
 */
public record LoginResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn,
    UserInfo user
) {
    /**
     * Nested user profile. Created via {@link UserInfo#from(User)} so the
     * service layer never has to know about the field order.
     */
    public record UserInfo(
        UUID id,
        String email,
        String fullName,
        Role role,
        UUID branchId,
        UserStatus status,
        String phone
    ) {
        public static UserInfo from(User u) {
            return new UserInfo(
                u.getId(),
                u.getEmail(),
                u.getFullName(),
                u.getRole(),
                u.getBranchId(),
                u.getStatus(),
                u.getPhone()
            );
        }
    }
}