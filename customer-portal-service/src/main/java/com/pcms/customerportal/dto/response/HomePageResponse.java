package com.pcms.customerportal.dto.response;

import java.util.List;

/**
 * SHOP-HOME aggregate payload.
 *
 * <p>Composed of:
 * <ul>
 *   <li>heroBanners — from home_banners table (position='HERO')</li>
 *   <li>subPromos — from home_banners table (position='SUB_PROMO')</li>
 *   <li>bestSellers — top medicines 30 days (from order-service Feign)</li>
 *   <li>featuredCategories — from category-service Feign</li>
 *   <li>brands — from brands table</li>
 *   <li>healthQuizTeaser — static CTA for HEALTH-QUIZ-LIST</li>
 *   <li>videosTeaser — top 6 most-viewed videos</li>
 * </ul>
 */
public record HomePageResponse(
        List<BannerResponse> heroBanners,
        List<BannerResponse> subPromos,
        List<BestSellerResponse> bestSellers,
        List<CategoryTeaserResponse> featuredCategories,
        List<BrandResponse> brands,
        HealthQuizTeaserResponse healthQuizTeaser,
        List<VideoResponse> videosTeaser,
        List<QuickLinkResponse> quickLinks
) {

    public record BannerResponse(String id, String title, String imageUrl, String linkUrl) {}

    public record BestSellerResponse(String id, String slug, String name, java.math.BigDecimal price,
                                     String imageUrl, Long soldCount) {}

    public record CategoryTeaserResponse(String id, String slug, String name, String imageUrl, Long productCount) {}

    public record BrandResponse(String id, String name, String logoUrl) {}

    public record HealthQuizTeaserResponse(boolean available, String url) {}

    public record VideoResponse(String id, String title, String thumbnailUrl, String youtubeId) {}

    public record QuickLinkResponse(String id, String label, String icon, String href) {}
}