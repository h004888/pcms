package com.pcms.branchservice.dto.response;

import java.util.UUID;

public record BranchStaffResponse(
        UUID id,
        String email,
        String fullName,
        String phone,
        String role,
        String status) {
}