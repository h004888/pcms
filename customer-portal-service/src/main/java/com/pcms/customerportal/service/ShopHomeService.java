package com.pcms.customerportal.service;

import com.pcms.customerportal.dto.response.HomePageResponse;

import java.util.UUID;

/**
 * Aggregates hero banners, best sellers, categories, videos, and teasers
 * for the B2C landing page (SHOP-HOME).
 *
 * <p>This is a read-mostly composition: data is fetched from local tables
 * (banners, videos) and from upstream services via Feign (catalog, category,
 * order). Returns a complete snapshot in &lt; 500ms p95.
 */
public interface ShopHomeService {

    /**
     * Build the home page payload.
     *
     * @param customerId optional - when provided, can return personalized sections
     *                   (e.g. recently-viewed). Currently unused, reserved for Sprint 6.
     * @return composed home page DTO
     */
    HomePageResponse buildHomePage(UUID customerId);
}