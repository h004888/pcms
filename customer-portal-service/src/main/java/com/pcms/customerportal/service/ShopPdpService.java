package com.pcms.customerportal.service;

import com.pcms.customerportal.dto.response.ProductDetailResponse;

import java.util.UUID;

/**
 * Aggregates product details for SHOP-PDP.
 *
 * <p>Combines catalog-service medicine data with:
 * <ul>
 *   <li>product_reviews (local) — average rating &amp; count</li>
 *   <li>inventory-service (Feign) — stock by branch</li>
 *   <li>related products — same category</li>
 * </ul>
 */
public interface ShopPdpService {

    ProductDetailResponse getProductDetail(UUID medicineId);
}