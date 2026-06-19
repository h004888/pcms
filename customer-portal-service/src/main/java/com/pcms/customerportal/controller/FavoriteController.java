package com.pcms.customerportal.controller;

import com.pcms.common.dto.PageResponse;
import com.pcms.customerportal.dto.request.AddFavoriteRequest;
import com.pcms.customerportal.dto.response.FavoriteResponse;
import com.pcms.customerportal.security.CurrentUser;
import com.pcms.customerportal.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/favorites")
@Tag(name = "UC14 - Customer Account / Favorites (Sản phẩm yêu thích)")
public class FavoriteController {

    private final FavoriteService service;

    public FavoriteController(FavoriteService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List my favorite medicines")
    public ResponseEntity<PageResponse<FavoriteResponse>> list(
            @RequestHeader(CurrentUser.USER_ID_HEADER) String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.list(CurrentUser.requireCustomerId(userId), page, size));
    }

    @PostMapping
    @Operation(summary = "Add a medicine to favorites (idempotent)")
    public ResponseEntity<FavoriteResponse> add(
            @RequestHeader(CurrentUser.USER_ID_HEADER) String userId,
            @Valid @RequestBody AddFavoriteRequest request) {
        return ResponseEntity.ok(service.add(CurrentUser.requireCustomerId(userId), request));
    }

    @DeleteMapping("/{medicineId}")
    @Operation(summary = "Remove a medicine from favorites")
    public ResponseEntity<Void> remove(
            @RequestHeader(CurrentUser.USER_ID_HEADER) String userId,
            @PathVariable("medicineId") UUID medicineId) {
        service.remove(CurrentUser.requireCustomerId(userId), medicineId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{medicineId}/check")
    @Operation(summary = "Check if a medicine is in my favorites")
    public ResponseEntity<Map<String, Object>> check(
            @RequestHeader(CurrentUser.USER_ID_HEADER) String userId,
            @PathVariable("medicineId") UUID medicineId) {
        boolean isFav = service.isFavorite(CurrentUser.requireCustomerId(userId), medicineId);
        return ResponseEntity.ok(Map.of("medicineId", medicineId, "isFavorite", isFav));
    }
}
