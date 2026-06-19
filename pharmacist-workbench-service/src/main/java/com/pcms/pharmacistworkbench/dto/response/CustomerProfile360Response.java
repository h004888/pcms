package com.pcms.pharmacistworkbench.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response for GET /rx/customers/{id}/profile-360.
 * Aggregated from customer-service + prescription-service + ai-engine.
 */
public record CustomerProfile360Response(
        UUID customerId,
        String name,
        String phone,
        String email,
        String tier,
        Integer loyaltyScore,
        List<String> allergies,
        List<String> chronicConditions,
        List<Object> recentOrders,
        List<Object> recentPrescriptions,
        String preferredPharmacist,
        String aiSummary,
        LocalDateTime lastVisit
) {}
