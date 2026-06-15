package com.pcms.userservice.controller;

import com.pcms.userservice.entity.User;
import com.pcms.userservice.enums.Role;
import com.pcms.userservice.enums.UserStatus;
import com.pcms.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * UC02 - Manage Users
 * Authorization: Admin/CEO full access, Branch Manager read-only per SRS §3.1.3
 */
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    /** GET /api/v1/users - List with search + pagination */
    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) UUID branchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending());
        Page<User> users = userRepository.searchUsers(search, role, branchId, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("data", users.getContent());
        response.put("page", users.getNumber());
        response.put("size", users.getSize());
        response.put("total", users.getTotalElements());
        response.put("totalPages", users.getTotalPages());
        return ResponseEntity.ok(response);
    }

    /** GET /api/v1/users/{id} - Get by ID */
    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable UUID id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** POST /api/v1/users - Create user (Admin/CEO only) */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("code", "MSG33", "message", "Email already exists"));
        }
        if (user.getStatus() == null) user.setStatus(UserStatus.ACTIVE);
        User saved = userRepository.save(user);
        return ResponseEntity.ok(saved);
    }

    /** PUT /api/v1/users/{id} - Update user */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody User details) {
        Optional<User> optional = userRepository.findById(id);
        if (optional.isEmpty()) return ResponseEntity.notFound().build();

        User user = optional.get();
        // Don't allow password update through this endpoint
        user.setFullName(details.getFullName());
        user.setPhone(details.getPhone());
        user.setRole(details.getRole());
        user.setBranchId(details.getBranchId());
        user.setStatus(details.getStatus());
        return ResponseEntity.ok(userRepository.save(user));
    }

    /** DELETE /api/v1/users/{id} - Soft delete (FR2.5) */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> softDelete(@PathVariable UUID id) {
        Optional<User> optional = userRepository.findById(id);
        if (optional.isEmpty()) return ResponseEntity.notFound().build();

        User user = optional.get();
        user.setStatus(UserStatus.INACTIVE);  // Soft delete per CR-08
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("code", "MSG06", "message", "User deactivated successfully"));
    }

    /** GET /api/v1/users/role/{role} - Get users by role */
    @GetMapping("/role/{role}")
    public List<User> findByRole(@PathVariable Role role) {
        return userRepository.findByRole(role);
    }
}
