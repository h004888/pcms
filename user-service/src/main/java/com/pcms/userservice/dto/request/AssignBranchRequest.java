package com.pcms.userservice.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request payload for {@code PUT /api/v1/users/{id}/branch} (TICKET-106,
 * FR2.3 - assign a user to a specific branch).
 *
 * <p>{@code branchId} may be {@code null} to un-assign (e.g. for Admin
 * users who are not bound to a single branch). When set, the value is
 * validated against the branch-service via the gateway before being
 * persisted.
 */
public record AssignBranchRequest(
        @NotNull(message = "branchId không được để trống")
        UUID branchId
) {
}
