package com.pcms.userservice.controller;

import com.pcms.userservice.dto.request.AssignBranchRequest;
import com.pcms.userservice.dto.request.ChangeUserRoleRequest;
import com.pcms.userservice.dto.request.ChangeUserStatusRequest;
import com.pcms.userservice.dto.request.CreateUserRequest;
import com.pcms.userservice.dto.request.UpdateUserRequest;
import com.pcms.userservice.dto.response.PageResponse;
import com.pcms.userservice.dto.response.UserResponse;
import com.pcms.userservice.enums.Role;
import com.pcms.userservice.enums.UserStatus;
import com.pcms.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * UC02 - Manage Users
 * Thin controller: delegates business logic to UserService.
 * Authorization: Admin/CEO full access, Branch Manager read-only per SRS §3.1.3
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** GET /api/v1/users - List with search + pagination */
    @GetMapping
    public ResponseEntity<PageResponse<UserResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) UUID branchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100),
                Sort.by("createdAt").descending());
        Page<UserResponse> users = userService.list(search, role, branchId, status, pageable);
        return ResponseEntity.ok(PageResponse.from(users));
    }

    /** GET /api/v1/users/export - Export filtered users as CSV. */
    @GetMapping("/export")
    public ResponseEntity<String> export(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) UUID branchId) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(userService.exportCsv(search, role, branchId, status));
    }

    /** GET /api/v1/users/{id} - Get by ID */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    /**
     * POST /api/v1/users - Create user (Admin/CEO only).
     * A random temporary password is generated server-side and hashed; in
     * production
     * it would be sent to the user via email.
     */
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        UserResponse created = userService.create(request, request.password());
        return ResponseEntity.ok(created);
    }

    /** PUT /api/v1/users/{id} - Update user */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    /** PUT /api/v1/users/{id}/role - Change user role. */
    @PutMapping("/{id}/role")
    public ResponseEntity<UserResponse> changeRole(@PathVariable UUID id,
            @Valid @RequestBody ChangeUserRoleRequest request) {
        return ResponseEntity.ok(userService.changeRole(id, request.role()));
    }

    /** PUT /api/v1/users/{id}/status - Change user status. */
    @PutMapping("/{id}/status")
    public ResponseEntity<UserResponse> changeStatus(@PathVariable UUID id,
            @Valid @RequestBody ChangeUserStatusRequest request) {
        return ResponseEntity.ok(userService.changeStatus(id, request.status()));
    }

    /** POST /api/v1/users/{id}/unlock - Clear BR05 account lock. */
    @PostMapping("/{id}/unlock")
    public ResponseEntity<UserResponse> unlock(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.unlock(id));
    }

    /** DELETE /api/v1/users/{id} - Soft delete (FR2.5) */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable UUID id) {
        userService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * PUT /api/v1/users/{id}/branch - TICKET-106 (FR2.3).
     * Assigns a user to a branch. Requires Admin role - enforced at the
     * API Gateway via JWT claims. The actor id (admin performing the
     * change) is captured from {@code X-User-Id} for the audit trail.
     */
    @PutMapping("/{id}/branch")
    public ResponseEntity<UserResponse> assignBranch(@PathVariable UUID id,
            @Valid @RequestBody AssignBranchRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String actorIdHeader) {
        return ResponseEntity.ok(userService.assignBranch(id, request, actorIdHeader));
    }

    /** GET /api/v1/users/role/{role} - Get users by role */
    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserResponse>> findByRole(@PathVariable Role role) {
        // No pagination in the original endpoint; use a large page to return all
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by("createdAt").descending());
        Page<UserResponse> page = userService.list(null, role, null, null, pageable);
        return ResponseEntity.ok(page.getContent());
    }

    private static String generateTempPassword() {
        byte[] bytes = new byte[12];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
