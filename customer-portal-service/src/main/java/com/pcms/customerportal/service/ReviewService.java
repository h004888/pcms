package com.pcms.customerportal.service;

import com.pcms.customerportal.dto.request.ReviewRequest;
import com.pcms.customerportal.dto.response.ReviewResponse;

import java.util.List;
import java.util.UUID;

public interface ReviewService {
    /**
     * SPRINT 3 - T11: List reviews for 1 medicine (public).
     */
    List<ReviewResponse> getByMedicine(UUID medicineId);

    /**
     * SPRINT 3 - T11: List reviews written by current customer (auth).
     */
    List<ReviewResponse> getMine(UUID customerId);

    /**
     * SPRINT 3 - T11: Create or update a review (1 / customer / medicine).
     * Idempotent: if exists, update rating+comment.
     */
    ReviewResponse upsert(UUID customerId, ReviewRequest request);
}