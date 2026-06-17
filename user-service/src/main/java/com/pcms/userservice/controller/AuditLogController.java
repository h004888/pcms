package com.pcms.userservice.controller;

import com.pcms.userservice.dto.response.AuditLogResponse;
import com.pcms.userservice.dto.response.PageResponse;
import com.pcms.userservice.service.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** UC02 audit log API for admin/security review. */
@RestController
@RequestMapping("/audit-logs")
public class AuditLogController {

    private final UserService userService;

    public AuditLogController(UserService userService) {
        this.userService = userService;
    }

    /** GET /api/v1/audit-logs - Audit log search. */
    @GetMapping
    public ResponseEntity<PageResponse<AuditLogResponse>> list(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending());
        return ResponseEntity.ok(PageResponse.from(userService.auditLogs(userId, action, pageable)));
    }
}