package com.pcms.userservice.controller;

import com.pcms.userservice.dto.request.SyncProfileRequest;
import com.pcms.userservice.dto.response.UserResponse;
import com.pcms.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
public class ProfileSyncController {

    private final UserService userService;

    public ProfileSyncController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<UserResponse> syncProfile(
            @PathVariable UUID id,
            @Valid @RequestBody SyncProfileRequest request) {
        return ResponseEntity.ok(userService.syncProfile(id, request));
    }
}
