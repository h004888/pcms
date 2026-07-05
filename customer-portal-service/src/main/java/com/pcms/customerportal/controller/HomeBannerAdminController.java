package com.pcms.customerportal.controller;

import com.pcms.common.dto.PageResponse;
import com.pcms.customerportal.dto.request.CreateHomeBannerRequest;
import com.pcms.customerportal.dto.request.UpdateHomeBannerRequest;
import com.pcms.customerportal.dto.response.HomeBannerAdminResponse;
import com.pcms.customerportal.enums.BannerStatus;
import com.pcms.customerportal.service.HomeBannerAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Admin CRUD for home_banners (UC14). Role ADMIN required.
 */
@RestController
@RequestMapping("/api/v1/admin/home-banners")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "UC14 - HomeBanner Admin (CRUD)")
public class HomeBannerAdminController {

    private final HomeBannerAdminService service;

    public HomeBannerAdminController(HomeBannerAdminService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List all banners (paginated, optional status filter)")
    public ResponseEntity<PageResponse<HomeBannerAdminResponse>> list(
            @RequestParam(required = false) BannerStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.list(status, page, size));
    }

    @PostMapping
    @Operation(summary = "Create a new banner")
    public ResponseEntity<HomeBannerAdminResponse> create(@Valid @RequestBody CreateHomeBannerRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update banner (partial - non-null fields)")
    public ResponseEntity<HomeBannerAdminResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateHomeBannerRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete (set status=DELETED)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
