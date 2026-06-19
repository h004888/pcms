package com.pcms.customerportal.service.impl;

import com.pcms.customerportal.client.CategoryClient;
import com.pcms.customerportal.dto.response.HomePageResponse;
import com.pcms.customerportal.dto.response.HomePageResponse.BannerResponse;
import com.pcms.customerportal.dto.response.HomePageResponse.BestSellerResponse;
import com.pcms.customerportal.dto.response.HomePageResponse.BrandResponse;
import com.pcms.customerportal.dto.response.HomePageResponse.CategoryTeaserResponse;
import com.pcms.customerportal.dto.response.HomePageResponse.HealthQuizTeaserResponse;
import com.pcms.customerportal.dto.response.HomePageResponse.VideoResponse;
import com.pcms.customerportal.entity.HomeBanner;
import com.pcms.customerportal.entity.Video;
import com.pcms.customerportal.enums.BannerStatus;
import com.pcms.customerportal.repository.HomeBannerRepository;
import com.pcms.customerportal.repository.VideoRepository;
import com.pcms.customerportal.service.ShopHomeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Default implementation of ShopHomeService.
 *
 * <p>Best-sellers / orders aggregation is intentionally a stub. Per the
 * plan, once order-service exposes a top-N-Medicines endpoint, this can
 * call it via Feign (deferred to Sprint 5 - order tracking phase).
 *
 * <p>Brands stub is also empty until a brand entity is added in a later sprint.
 */
@Service
public class ShopHomeServiceImpl implements ShopHomeService {

    private static final Logger log = LoggerFactory.getLogger(ShopHomeServiceImpl.class);

    private final HomeBannerRepository bannerRepo;
    private final VideoRepository videoRepo;
    private final CategoryClient categoryClient;

    public ShopHomeServiceImpl(HomeBannerRepository bannerRepo,
                               VideoRepository videoRepo,
                               CategoryClient categoryClient) {
        this.bannerRepo = bannerRepo;
        this.videoRepo = videoRepo;
        this.categoryClient = categoryClient;
    }

    @Override
    public HomePageResponse buildHomePage(UUID customerId) {
        log.debug("Building SHOP-HOME payload (customerId={})", customerId);
        return new HomePageResponse(
                loadHeroBanners(),
                loadBestSellers(),
                loadFeaturedCategories(),
                loadBrands(),
                new HealthQuizTeaserResponse(true, "/health-quiz"),
                loadVideosTeaser()
        );
    }

    private List<BannerResponse> loadHeroBanners() {
        try {
            return bannerRepo.findVisible(BannerStatus.ACTIVE, LocalDateTime.now())
                    .stream()
                    .map(b -> new BannerResponse(b.getId().toString(), b.getTitle(),
                            b.getImageUrl(), b.getLinkUrl()))
                    .toList();
        } catch (Exception e) {
            log.warn("Failed to load home banners: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Stub: would aggregate from order-service top-N medicines in the last 30 days.
     * TODO(sprint5): wire OrderClient.feignTopSellers(period=30d, limit=12)
     */
    private List<BestSellerResponse> loadBestSellers() {
        log.debug("bestSellers: stub (order-service aggregation pending)");
        return List.of();
    }

    private List<CategoryTeaserResponse> loadFeaturedCategories() {
        try {
            List<Map<String, Object>> raw = categoryClient.list(0, 50);
            return raw.stream()
                    .limit(6)
                    .map(this::toCategoryTeaser)
                    .toList();
        } catch (Exception e) {
            log.warn("Failed to load featured categories from category-service: {}", e.getMessage());
            return List.of();
        }
    }

    private CategoryTeaserResponse toCategoryTeaser(Map<String, Object> m) {
        Object id = m.get("id");
        Object name = m.getOrDefault("name", "");
        Object image = m.getOrDefault("imageUrl", "");
        return new CategoryTeaserResponse(
                id != null ? id.toString() : null,
                name.toString(),
                image.toString(),
                0L // productCount: will be enriched once catalog-service exposes count endpoint
        );
    }

    private List<BrandResponse> loadBrands() {
        // Stub: brand entity not yet introduced in this service.
        return List.of();
    }

    private List<VideoResponse> loadVideosTeaser() {
        try {
            return videoRepo.findTop6ByStatusOrderByCreatedAtDesc("ACTIVE")
                    .stream()
                    .map(this::toVideoTeaser)
                    .toList();
        } catch (Exception e) {
            log.warn("Failed to load video teasers: {}", e.getMessage());
            return List.of();
        }
    }

    private VideoResponse toVideoTeaser(Video v) {
        return new VideoResponse(
                v.getId().toString(),
                v.getTitle(),
                v.getThumbnailUrl(),
                v.getYoutubeId()
        );
    }

    /** Best-seller price helper for future use when wiring OrderClient */
    @SuppressWarnings("unused")
    private static BigDecimal zeroPrice() { return BigDecimal.ZERO; }
}