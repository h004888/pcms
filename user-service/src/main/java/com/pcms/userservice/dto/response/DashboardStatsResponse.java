package com.pcms.userservice.dto.response;

/** Dashboard KPI response for SCR-HOME. */
public record DashboardStatsResponse(
        long totalUsers,
        long activeUsers,
        long lockedUsers,
        long inactiveUsers,
        long branchManagers,
        long pharmacists,
        long administrators) {
}