package com.pcms.customerportal.service.impl;

import com.pcms.customerportal.client.CategoryClient;
import com.pcms.customerportal.client.OrderClient;
import com.pcms.customerportal.dto.response.HomePageResponse;
import com.pcms.customerportal.dto.response.HomePageResponse.BannerResponse;
import com.pcms.customerportal.dto.response.HomePageResponse.BestSellerResponse;
import com.pcms.customerportal.dto.response.HomePageResponse.BrandResponse;
import com.pcms.customerportal.dto.response.HomePageResponse.CategoryTeaserResponse;
import com.pcms.customerportal.dto.response.HomePageResponse.HealthQuizTeaserResponse;
import com.pcms.customerportal.dto.response.HomePageResponse.QuickLinkResponse;
import com.pcms.customerportal.dto.response.HomePageResponse.VideoResponse;
import com.pcms.customerportal.entity.Brand;
import com.pcms.customerportal.entity.HomeBanner;
import com.pcms.customerportal.entity.Video;
import com.pcms.customerportal.enums.BannerStatus;
import com.pcms.customerportal.repository.BrandRepository;
import com.pcms.customerportal.repository.HomeBannerRepository;
import com.pcms.customerportal.repository.QuickLinkRepository;
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
 */
@Service
public class ShopHomeServiceImpl implements ShopHomeService {

    private static final Logger log = LoggerFactory.getLogger(ShopHomeServiceImpl.class);

    private final HomeBannerRepository bannerRepo;
    private final VideoRepository videoRepo;
    private final CategoryClient categoryClient;
    private final BrandRepository brandRepository;
    private final OrderClient orderClient;
    private final QuickLinkRepository quickLinkRepository;

    public ShopHomeServiceImpl(HomeBannerRepository bannerRepo,
                               VideoRepository videoRepo,
                               CategoryClient categoryClient,
                               BrandRepository brandRepository,
                               OrderClient orderClient,
                               QuickLinkRepository quickLinkRepository) {
        this.bannerRepo = bannerRepo;
        this.videoRepo = videoRepo;
        this.categoryClient = categoryClient;
        this.brandRepository = brandRepository;
        this.orderClient = orderClient;
        this.quickLinkRepository = quickLinkRepository;
    }

    @Override
    public HomePageResponse buildHomePage(UUID customerId) {
        log.debug("Building SHOP-HOME payload (customerId={})", customerId);
        return new HomePageResponse(
                loadHeroBanners(),
                loadSubPromos(),
                loadBestSellers(),
                loadFeaturedCategories(),
                loadBrands(),
                new HealthQuizTeaserResponse(true, "/health-quiz"),
                loadVideosTeaser(),
                loadQuickLinks()
        );
    }

    private List<BannerResponse> loadHeroBanners() {
        try {
            return bannerRepo.findVisibleByPosition(BannerStatus.ACTIVE, "HERO", LocalDateTime.now())
                    .stream()
                    .map(b -> new BannerResponse(b.getId().toString(), b.getTitle(),
                            b.getImageUrl(), b.getLinkUrl()))
                    .toList();
        } catch (Exception e) {
            log.warn("Failed to load hero banners: {}", e.getMessage());
            return List.of();
        }
    }

    private List<BannerResponse> loadSubPromos() {
        try {
            return bannerRepo.findVisibleByPosition(BannerStatus.ACTIVE, "SUB_PROMO", LocalDateTime.now())
                    .stream()
                    .map(b -> new BannerResponse(b.getId().toString(), b.getTitle(),
                            b.getImageUrl(), b.getLinkUrl()))
                    .toList();
        } catch (Exception e) {
            log.warn("Failed to load sub-promos: {}", e.getMessage());
            return List.of();
        }
    }

    private List<BestSellerResponse> loadBestSellers() {
        try {
            List<Map<String, Object>> raw = orderClient.getTopMedicines(30, 10);
            return raw.stream()
                    .map(this::toBestSeller)
                    .toList();
        } catch (Exception e) {
            log.warn("Failed to load best sellers from order-service: {}", e.getMessage());
            return List.of();
        }
    }

    private BestSellerResponse toBestSeller(Map<String, Object> m) {
        Object id = m.get("medicineId");
        Object name = m.getOrDefault("medicineName", "");
        Object price = m.getOrDefault("price", 0);
        Object soldCount = m.getOrDefault("soldCount", 0);
        return new BestSellerResponse(
                id != null ? id.toString() : null,
                "",  // slug: enriched later from catalog-service
                name.toString(),
                price instanceof Number ? java.math.BigDecimal.valueOf(((Number) price).doubleValue()) : java.math.BigDecimal.ZERO,
                "",  // imageUrl: enriched later from catalog-service
                soldCount instanceof Number ? ((Number) soldCount).longValue() : 0L
        );
    }

    private List<CategoryTeaserResponse> loadFeaturedCategories() {
        try {
            var page = categoryClient.list(0, 50);
            return page.data().stream()
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
        Object slug = m.get("slug");
        Object name = m.getOrDefault("name", "");
        Object image = m.getOrDefault("imageUrl", "");
        return new CategoryTeaserResponse(
                id != null ? id.toString() : null,
                slug != null ? slug.toString() : null,
                name.toString(),
                image.toString(),
                0L // productCount: will be enriched once catalog-service exposes count endpoint
        );
    }

    private List<BrandResponse> loadBrands() {
        try {
            return brandRepository.findByStatusOrderBySortOrderAsc("ACTIVE")
                    .stream()
                    .map(b -> new BrandResponse(
                            b.getId().toString(),
                            b.getName(),
                            b.getLogoUrl()))
                    .toList();
        } catch (Exception e) {
            log.warn("Failed to load brands: {}", e.getMessage());
            return List.of();
        }
    }

    private List<QuickLinkResponse> loadQuickLinks() {
        try {
            return quickLinkRepository.findByStatusOrderBySortOrderAsc("ACTIVE")
                    .stream()
                    .map(ql -> new QuickLinkResponse(
                            ql.getId().toString(),
                            ql.getLabel(),
                            ql.getIcon(),
                            ql.getHref()))
                    .toList();
        } catch (Exception e) {
            log.warn("Failed to load quick links: {}", e.getMessage());
            return List.of();
        }
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