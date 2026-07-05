package com.pcms.customerportal.controller;

import com.pcms.customerportal.dto.response.VideoResponse;
import com.pcms.customerportal.service.VideoAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public video endpoint for B2C shop.
 * GET /videos returns published (ACTIVE) videos for the storefront.
 */
@RestController
@RequestMapping("/videos")
@Tag(name = "UC14 - Public Videos")
public class VideoPublicController {

    private final VideoAdminService service;

    public VideoPublicController(VideoAdminService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List active videos for public storefront")
    public ResponseEntity<List<VideoResponse>> listPublic() {
        return ResponseEntity.ok(service.listActive());
    }
}
