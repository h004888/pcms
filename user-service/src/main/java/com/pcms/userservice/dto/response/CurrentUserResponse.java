package com.pcms.userservice.dto.response;

import com.pcms.userservice.enums.Role;
import com.pcms.userservice.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response payload for {@code GET /api/v1/auth/me}.
 *
 * <p>Contains the currently authenticated user's profile plus the effective
 * permission list derived from {@link Role}. Used by the frontend to render
 * the role-based dashboard (SCR-HOME) without an extra /users/{id} call.
 */
public record CurrentUserResponse(
        UUID id,
        String email,
        String fullName,
        String phone,
        Role role,
        UUID branchId,
        UserStatus status,
        Boolean emailVerified,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt,
        List<String> permissions
) {
    /**
     * Map Role to a stable list of permission keys (used by FE for menu
     * visibility and action-level guards). Mirrors SRS v1.3.0 §3.1.3.
     */
    public static List<String> permissionsFor(Role role) {
        if (role == null) {
            return List.of();
        }
        return switch (role) {
            case ADMIN -> List.of(
                    "USER_MGMT", "BRANCH_MGMT", "MEDICINE_MGMT", "CATEGORY_MGMT",
                    "SUPPLIER_MGMT", "INVENTORY_MGMT", "ORDER_MGMT", "PAYMENT_MGMT",
                    "CUSTOMER_MGMT", "REPORT_VIEW", "PRESCRIPTION_MGMT", "NOTIF_MGMT",
                    "AUDIT_VIEW");
            case CEO -> List.of(
                    "USER_MGMT", "BRANCH_MGMT", "MEDICINE_MGMT", "CATEGORY_MGMT",
                    "SUPPLIER_MGMT", "INVENTORY_MGMT", "ORDER_MGMT", "PAYMENT_MGMT",
                    "CUSTOMER_MGMT", "REPORT_VIEW", "PRESCRIPTION_MGMT", "NOTIF_MGMT",
                    "AUDIT_VIEW");
            case BRANCH_MANAGER -> List.of(
                    "INVENTORY_MGMT", "ORDER_MGMT", "REPORT_VIEW_OWN_BRANCH",
                    "CUSTOMER_VIEW", "PRESCRIPTION_VIEW", "NOTIF_VIEW");
            case PHARMACIST -> List.of(
                    "MEDICINE_MGMT", "INVENTORY_VIEW", "ORDER_MGMT", "PAYMENT_PROCESS",
                    "CUSTOMER_MGMT", "PRESCRIPTION_MGMT", "NOTIF_VIEW");
            case CUSTOMER -> List.of(
                    "ORDER_VIEW_OWN", "PROFILE_SELF_MGMT", "PRESCRIPTION_VIEW_OWN");
        };
    }
}
