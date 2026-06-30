package com.pcms.customerportal.controller;

import com.pcms.customerportal.dto.request.ReviewRequest;
import com.pcms.customerportal.dto.response.ReviewResponse;
import com.pcms.customerportal.security.CurrentUser;
import com.pcms.customerportal.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * SPRINT 3 - T11: Reviews API (UC14).
 * 3 endpoints:
 *   GET  /reviews?medicineId={uuid}  — public, list reviews for 1 medicine
 *   GET  /reviews/me                 — auth, list my reviews
 *   POST /reviews                     — auth (X-User-Id), create or update
 *
 * Upsert semantics: 1 review per (customer, medicine). Repeat POST sẽ update.
 */
@RestController
@RequestMapping("/reviews")
@Tag(name = "UC14 - Reviews")
public class ReviewsController {

    private final ReviewService service;

    public ReviewsController(ReviewService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List reviews for a medicine (public)")
    public ResponseEntity<List<ReviewResponse>> listByMedicine(
            @RequestParam UUID medicineId) {
        return ResponseEntity.ok(service.getByMedicine(medicineId));
    }

    @GetMapping("/me")
    @Operation(summary = "List reviews I wrote (auth)")
    public ResponseEntity<List<ReviewResponse>> myReviews(
            @RequestHeader(CurrentUser.USER_ID_HEADER) String userId) {
        UUID customerId = CurrentUser.requireCustomerId(userId);
        return ResponseEntity.ok(service.getMine(customerId));
    }

    @PostMapping
    @Operation(summary = "Create or update my review (auth, X-User-Id)")
    public ResponseEntity<ReviewResponse> create(
            @RequestHeader(CurrentUser.USER_ID_HEADER) String userId,
            @Valid @RequestBody ReviewRequest request) {
        UUID customerId = CurrentUser.requireCustomerId(userId);
        ReviewResponse saved = service.upsert(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}