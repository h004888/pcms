package com.pcms.userservice.dto.response;

import com.pcms.userservice.entity.AuditLog;

import java.time.LocalDateTime;
import java.util.UUID;

/** Audit log response DTO. */
public record AuditLogResponse(
        UUID id,
        UUID userId,
        String action,
        UUID targetId,
        String ipAddress,
        String description,
        LocalDateTime createdAt) {
    public static AuditLogResponse from(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getUserId(),
                auditLog.getAction(),
                auditLog.getTargetId(),
                auditLog.getIpAddress(),
                auditLog.getDescription(),
                auditLog.getCreatedAt());
    }
}