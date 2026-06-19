package com.pcms.customerportal.controller;

import com.pcms.common.dto.PageResponse;
import com.pcms.customerportal.dto.request.ScanCodeRequest;
import com.pcms.customerportal.dto.response.DiseaseInfoResponse;
import com.pcms.customerportal.dto.response.HealthArticleResponse;
import com.pcms.customerportal.dto.response.VerifyOriginResponse;
import com.pcms.customerportal.service.HealthContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "UC14 - Health Content + Verify Origin")
public class HealthContentController {

    private final HealthContentService service;

    public HealthContentController(HealthContentService service) {
        this.service = service;
    }

    @GetMapping("/health-articles")
    @Operation(summary = "List published health articles (optional category filter)")
    public ResponseEntity<PageResponse<HealthArticleResponse>> listArticles(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.listArticles(category, page, size));
    }

    @GetMapping("/health-articles/{slug}")
    @Operation(summary = "Get a health article by slug (auto-increment view count)")
    public ResponseEntity<HealthArticleResponse> getArticle(@PathVariable String slug) {
        return ResponseEntity.ok(service.getArticleBySlug(slug));
    }

    @GetMapping("/diseases")
    @Operation(summary = "List disease info (optional audience/season filters)")
    public ResponseEntity<PageResponse<DiseaseInfoResponse>> listDiseases(
            @RequestParam(required = false) String audience,
            @RequestParam(required = false) String season,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.listDiseases(audience, season, page, size));
    }

    @PostMapping("/verify-origin/scan")
    @Operation(summary = "Scan QR/barcode to verify drug origin (SHOP-VERIFY-ORIGIN)")
    public ResponseEntity<VerifyOriginResponse> verifyOrigin(@Valid @RequestBody ScanCodeRequest request) {
        return ResponseEntity.ok(service.verifyOrigin(request));
    }
}