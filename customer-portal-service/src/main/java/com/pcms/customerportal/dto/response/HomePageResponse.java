package com.pcms.customerportal.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * SHOP-HOME aggregate payload.
 *
 * <p>Composed of:
 * <ul>
 *   <li>heroBanners — from home_banners table (position='HERO')</li>
 *   <li>bestSellers — top medicines 30 days (from order-service Feign)</li>
 *   <li>featuredCategories — from category-service Feign</li>
 *   <li>flashSales — active flash sales with items</li>
 *   <li>quickLinks — from quick_links table</li>
 * </ul>
 */
public record HomePageResponse(
        List<BannerResponse> heroBanners,
        List<BestSellerResponse> bestSellers,
        List<CategoryTeaserResponse> featuredCategories,
        List<QuickLinkResponse> quickLinks,
        List<FlashSaleTeaserResponse> flashSales
) {

    public record BannerResponse(String id, String title, String imageUrl, String linkUrl) {}

    public record BestSellerResponse(String id, String slug, String name, BigDecimal price,
                                     String imageUrl, Long soldCount,
                                     String description, Double rating, Long reviewCount) {}

    public record CategoryTeaserResponse(String id, String slug, String name, String imageUrl, Long productCount) {}

    public record QuickLinkResponse(String id, String label, String icon, String href) {}

    public record FlashSaleTeaserResponse(String id, String name, String description,
                                          BigDecimal discountPct, String startsAt, String endsAt,
                                          List<FlashSaleItemResponse> items) {}

    public record FlashSaleItemResponse(String id, String medicineName,
                                        BigDecimal originalPrice, BigDecimal salePrice,
                                        String imageUrl, int qtyLimit, int soldQty) {}
}