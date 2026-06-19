package com.pcms.customerportal.dto.response;

import java.util.List;

/**
 * SHOP-HOME aggregate payload.
 *
 * <p>Composed of:
 * <ul>
 *   <li>heroBanners — from home_banners table</li>
 *   <li>bestSellers — top medicines 30 days (placeholder until order-service Feign added)</li>
 *   <li>featuredCategories — from category-service Feign</li>
 *   <li>brands — placeholder for now</li>
 *   <li>healthQuizTeaser — static CTA for HEALTH-QUIZ-LIST</li>
 *   <li>videosTeaser — top 6 most-viewed videos</li>
 * </ul>
 */
public record HomePageResponse(
        List<BannerResponse> heroBanners,
        List<BestSellerResponse> bestSellers,
        List<CategoryTeaserResponse> featuredCategories,
        List<BrandResponse> brands,
        HealthQuizTeaserResponse healthQuizTeaser,
        List<VideoResponse> videosTeaser
) {

    public record BannerResponse(String id, String title, String imageUrl, String linkUrl) {}

    public record BestSellerResponse(String id, String name, java.math.BigDecimal price,
                                     String imageUrl, Long soldCount) {}

    public record CategoryTeaserResponse(String id, String name, String imageUrl, Long productCount) {}

    public record BrandResponse(String id, String name, String logoUrl) {}

    public record HealthQuizTeaserResponse(boolean available, String url) {}

    public record VideoResponse(String id, String title, String thumbnailUrl, String youtubeId) {}
}