package com.pcms.customerportal.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * SHOP-PDP - product detail page aggregate payload.
 *
 * <p>Composed of:
 * <ul>
 *   <li>core fields (id, sku, name, category, price, unit, image, description)</li>
 *   <li>usage, ingredients</li>
 *   <li>review aggregate (avgRating, reviewCount)</li>
 *   <li>stockByBranch — from inventory-service Feign</li>
 *   <li>relatedProducts — same category medicines (top 5 by popularity)</li>
 * </ul>
 */
public record ProductDetailResponse(
        String id,
        String sku,
        String name,
        CategoryRef category,
        BigDecimal price,
        String unit,
        String imageUrl,
        String description,
        List<String> ingredients,
        String usage,
        Boolean prescriptionRequired,
        Double averageRating,
        Long reviewCount,
        List<StockByBranch> stockByBranch,
        List<RelatedProduct> relatedProducts
) {

    public record CategoryRef(String id, String name) {}

    public record StockByBranch(String branchId, String branchName, Integer qty) {}

    public record RelatedProduct(String id, String name, BigDecimal price, String imageUrl) {}
}