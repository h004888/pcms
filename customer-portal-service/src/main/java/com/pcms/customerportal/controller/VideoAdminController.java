package com.pcms.customerportal.controller;

import com.pcms.common.dto.PageResponse;
import com.pcms.customerportal.dto.request.CreateVideoRequest;
import com.pcms.customerportal.dto.request.UpdateVideoRequest;
import com.pcms.customerportal.dto.response.VideoResponse;
import com.pcms.customerportal.service.VideoAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * VideoAdmin - CRUD for video content (admin only, ecom-ops/UC19).
 */
@RestController
@RequestMapping("/admin/videos")
@Tag(name = "UC19 - Video Admin (CRUD)")
public class VideoAdminController {

    private final VideoAdminService service;

    public VideoAdminController(VideoAdminService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List all videos (including INACTIVE/DELETED) for admin")
    public ResponseEntity<PageResponse<VideoResponse>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.listAll(category, status, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get video by id")
    public ResponseEntity<VideoResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PostMapping
    @Operation(summary = "Create new video (YouTube embed)")
    public ResponseEntity<VideoResponse> create(@Valid @RequestBody CreateVideoRequest request) {
        return ResponseEntity.ok(service.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update video (partial update - only non-null fields)")
    public ResponseEntity<VideoResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateVideoRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete (status=DELETED)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}