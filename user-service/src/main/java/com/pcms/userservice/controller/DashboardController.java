package com.pcms.userservice.controller;

import com.pcms.userservice.dto.response.DashboardStatsResponse;
import com.pcms.userservice.dto.response.PageResponse;
import com.pcms.userservice.dto.response.UserResponse;
import com.pcms.userservice.service.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** UC01/UC02 dashboard API for SCR-HOME. */
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final UserService userService;

    public DashboardController(UserService userService) {
        this.userService = userService;
    }

    /** GET /api/v1/dashboard/stats - SCR-HOME KPI data. */
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> stats() {
        return ResponseEntity.ok(userService.dashboardStats());
    }

    /** GET /api/v1/dashboard/recent-logins - SCR-HOME recent login data. */
    @GetMapping("/recent-logins")
    public ResponseEntity<PageResponse<UserResponse>> recentLogins(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("lastLoginAt").descending());
        return ResponseEntity.ok(PageResponse.from(userService.recentLogins(pageable)));
    }
}