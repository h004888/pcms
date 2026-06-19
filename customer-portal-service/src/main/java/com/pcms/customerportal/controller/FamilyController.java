package com.pcms.customerportal.controller;

import com.pcms.customerportal.dto.request.CreateFamilyMemberRequest;
import com.pcms.customerportal.dto.request.UpdateFamilyMemberRequest;
import com.pcms.customerportal.dto.response.FamilyMemberResponse;
import com.pcms.customerportal.security.CurrentUser;
import com.pcms.customerportal.service.FamilyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/family")
@Tag(name = "UC14 - Customer Account / Family (Tài khoản gia đình)")
public class FamilyController {

    private final FamilyService service;

    public FamilyController(FamilyService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List my family members")
    public ResponseEntity<List<FamilyMemberResponse>> list(
            @RequestHeader(CurrentUser.USER_ID_HEADER) String userId) {
        return ResponseEntity.ok(service.list(CurrentUser.requireCustomerId(userId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get family member by id (owner only)")
    public ResponseEntity<FamilyMemberResponse> get(
            @RequestHeader(CurrentUser.USER_ID_HEADER) String userId,
            @PathVariable("id") UUID id) {
        return ResponseEntity.ok(service.get(CurrentUser.requireCustomerId(userId), id));
    }

    @PostMapping
    @Operation(summary = "Add a family member")
    public ResponseEntity<FamilyMemberResponse> create(
            @RequestHeader(CurrentUser.USER_ID_HEADER) String userId,
            @Valid @RequestBody CreateFamilyMemberRequest request) {
        return ResponseEntity.status(201)
                .body(service.create(CurrentUser.requireCustomerId(userId), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a family member")
    public ResponseEntity<FamilyMemberResponse> update(
            @RequestHeader(CurrentUser.USER_ID_HEADER) String userId,
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateFamilyMemberRequest request) {
        return ResponseEntity.ok(service.update(CurrentUser.requireCustomerId(userId), id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete a family member")
    public ResponseEntity<Void> delete(
            @RequestHeader(CurrentUser.USER_ID_HEADER) String userId,
            @PathVariable("id") UUID id) {
        service.softDelete(CurrentUser.requireCustomerId(userId), id);
        return ResponseEntity.noContent().build();
    }
}
