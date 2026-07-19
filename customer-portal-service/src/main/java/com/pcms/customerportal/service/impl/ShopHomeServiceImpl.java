package com.pcms.customerportal.service.impl;

import com.pcms.customerportal.client.CategoryClient;
import com.pcms.customerportal.client.OrderClient;
import com.pcms.customerportal.dto.response.HomePageResponse;
import com.pcms.customerportal.dto.response.HomePageResponse.BannerResponse;
import com.pcms.customerportal.dto.response.HomePageResponse.BestSellerResponse;
import com.pcms.customerportal.dto.response.HomePageResponse.CategoryTeaserResponse;
import com.pcms.customerportal.dto.response.HomePageResponse.FlashSaleItemResponse;
import com.pcms.customerportal.dto.response.HomePageResponse.FlashSaleTeaserResponse;
import com.pcms.customerportal.dto.response.HomePageResponse.QuickLinkResponse;
import com.pcms.customerportal.enums.BannerStatus;
import com.pcms.customerportal.repository.HomeBannerRepository;
import com.pcms.customerportal.repository.QuickLinkRepository;
import com.pcms.customerportal.service.ShopHomeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Default implementation of ShopHomeService.
 */
@Service
public class ShopHomeServiceImpl implements ShopHomeService {

    private static final Logger log = LoggerFactory.getLogger(ShopHomeServiceImpl.class);

    private final HomeBannerRepository bannerRepo;
    private final CategoryClient categoryClient;
    private final OrderClient orderClient;
    private final QuickLinkRepository quickLinkRepository;
    private final JdbcTemplate jdbcTemplate;

    public ShopHomeServiceImpl(HomeBannerRepository bannerRepo,
                               CategoryClient categoryClient,
                               OrderClient orderClient,
                               QuickLinkRepository quickLinkRepository,
                               DataSource dataSource) {
        this.bannerRepo = bannerRepo;
        this.categoryClient = categoryClient;
        this.orderClient = orderClient;
        this.quickLinkRepository = quickLinkRepository;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public HomePageResponse buildHomePage(UUID customerId) {
        log.debug("Building SHOP-HOME payload (customerId={})", customerId);
        return new HomePageResponse(
                loadHeroBanners(),
                loadBestSellers(),
                loadFeaturedCategories(),
                loadQuickLinks(),
                loadFlashSales()
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

    private List<BestSellerResponse> loadBestSellers() {
        try {
            List<Map<String, Object>> raw = orderClient.getTopMedicines(30, 10);
            if (raw != null && !raw.isEmpty()) {
                List<BestSellerResponse> list = raw.stream()
                        .map(this::toBestSeller)
                        .collect(Collectors.toList());
                enrichBestSellers(list);
                return list;
            }
        } catch (Exception e) {
            log.warn("Failed to load best sellers from order-service (Feign): {}", e.getMessage());
        }
        // Fallback: query order_items directly
        List<BestSellerResponse> fallback = loadBestSellersFallback();
        enrichBestSellers(fallback);
        return fallback;
    }

    private List<BestSellerResponse> loadBestSellersFallback() {
        try {
            log.info("Using fallback best-sellers query from pcms_order");
            var rows = jdbcTemplate.queryForList(
                "SELECT oi.medicine_id, oi.medicine_name, SUM(oi.qty) as sold_count, " +
                "MAX(m.price) as price, MAX(m.image_url) as image_url, " +
                "MAX(m.description) as description, " +
                "COALESCE(AVG(r.rating), 0) as avg_rating, " +
                "COUNT(r.id) as review_count " +
                "FROM pcms_order.order_items oi " +
                "JOIN pcms_catalog.medicines m ON oi.medicine_id = m.id " +
                "LEFT JOIN pcms_customer_portal.review r ON oi.medicine_id = r.medicine_id " +
                "GROUP BY oi.medicine_id, oi.medicine_name " +
                "ORDER BY sold_count DESC LIMIT 10"
            );
            return rows.stream()
                    .map(r -> new BestSellerResponse(
                        uuidBytesToString(r.get("medicine_id")),
                        "",
                        r.get("medicine_name").toString(),
                        r.get("price") != null ? new BigDecimal(r.get("price").toString()) : BigDecimal.ZERO,
                        r.get("image_url") != null ? r.get("image_url").toString() : "",
                        ((Number) r.get("sold_count")).longValue(),
                        r.get("description") != null ? r.get("description").toString() : "",
                        r.get("avg_rating") != null ? ((Number) r.get("avg_rating")).doubleValue() : null,
                        ((Number) r.get("review_count")).longValue()
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Fallback best-sellers query also failed: {}", e.getMessage());
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
                soldCount instanceof Number ? ((Number) soldCount).longValue() : 0L,
                "",  // description: enriched later via enrichBestSellers
                null,  // rating: enriched later via enrichBestSellers
                0L  // reviewCount: enriched later via enrichBestSellers
        );
    }

    private void enrichBestSellers(List<BestSellerResponse> list) {
        if (list == null || list.isEmpty()) return;
        List<String> ids = list.stream()
                .map(BestSellerResponse::id)
                .filter(Objects::nonNull)
                .toList();
        if (ids.isEmpty()) return;

        String inClause = ids.stream()
                .map(id -> "UUID_TO_BIN('" + id.replace("'", "''") + "')")
                .collect(Collectors.joining(","));

        // Batch 1: descriptions + slugs from pcms_catalog.medicines
        Map<String, String> descMap = new HashMap<>();
        Map<String, String> slugMap = new HashMap<>();
        try {
            var descRows = jdbcTemplate.queryForList(
                "SELECT BIN_TO_UUID(id) as id_str, description, slug FROM pcms_catalog.medicines WHERE id IN (" + inClause + ")");
            for (var row : descRows) {
                descMap.put(row.get("id_str").toString(),
                    row.get("description") != null ? row.get("description").toString() : "");
                slugMap.put(row.get("id_str").toString(),
                    row.get("slug") != null ? row.get("slug").toString() : "");
            }
        } catch (Exception e) {
            log.warn("Failed to enrich descriptions: {}", e.getMessage());
        }

        // Batch 2: rating + review_count from pcms_customer_portal.review
        Map<String, Double> ratingMap = new HashMap<>();
        Map<String, Long> reviewCountMap = new HashMap<>();
        try {
            var ratingRows = jdbcTemplate.queryForList(
                "SELECT BIN_TO_UUID(medicine_id) as med_id, AVG(rating) as avg_rating, COUNT(*) as review_count " +
                "FROM pcms_customer_portal.review WHERE medicine_id IN (" + inClause + ") " +
                "GROUP BY medicine_id");
            for (var row : ratingRows) {
                String medId = row.get("med_id").toString();
                ratingMap.put(medId, row.get("avg_rating") != null
                    ? ((Number) row.get("avg_rating")).doubleValue() : null);
                reviewCountMap.put(medId, ((Number) row.get("review_count")).longValue());
            }
        } catch (Exception e) {
            log.warn("Failed to enrich ratings: {}", e.getMessage());
        }

        // Replace each item with enriched version
        for (int i = 0; i < list.size(); i++) {
            var item = list.get(i);
            String itemId = item.id();
            list.set(i, new BestSellerResponse(
                item.id(), slugMap.getOrDefault(itemId, item.slug()), item.name(), item.price(), item.imageUrl(), item.soldCount(),
                descMap.getOrDefault(itemId, ""),
                ratingMap.get(itemId),
                reviewCountMap.getOrDefault(itemId, 0L)
            ));
        }
    }

    private List<CategoryTeaserResponse> loadFeaturedCategories() {
        try {
            var page = categoryClient.list(0, 50);
            // Build product count map from pcms_catalog.medicines
            Map<String, Long> countByCat = new HashMap<>();
            try {
                var rows = jdbcTemplate.queryForList(
                    "SELECT c.name, COUNT(m.id) as cnt FROM pcms_catalog.medicines m " +
                    "JOIN pcms_category.categories c ON m.category_id = c.id " +
                    "GROUP BY c.name");
                for (var row : rows) {
                    countByCat.put(row.get("name").toString(), ((Number) row.get("cnt")).longValue());
                }
            } catch (Exception e) {
                log.warn("Failed to load product counts: {}", e.getMessage());
            }
            final var finalCounts = countByCat;
            return page.data().stream()
                    .limit(6)
                    .map(m -> toCategoryTeaser(m, finalCounts.getOrDefault(m.get("name"), 0L)))
                    .toList();
        } catch (Exception e) {
            log.warn("Failed to load featured categories from category-service: {}", e.getMessage());
            return List.of();
        }
    }

    private CategoryTeaserResponse toCategoryTeaser(Map<String, Object> m, Long productCount) {
        Object id = m.get("id");
        Object slug = m.get("slug");
        Object name = m.getOrDefault("name", "");
        Object image = m.getOrDefault("imageUrl", "");
        return new CategoryTeaserResponse(
                id != null ? id.toString() : null,
                slug != null ? slug.toString() : null,
                name.toString(),
                image.toString(),
                productCount
        );
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

    private List<FlashSaleTeaserResponse> loadFlashSales() {
        try {
            var sales = jdbcTemplate.queryForList(
                "SELECT fs.id, fs.name, fs.description, fs.discount_pct, " +
                "fs.starts_at, fs.ends_at " +
                "FROM pcms_ecom_ops.flash_sales fs " +
                "WHERE fs.status = 'ACTIVE' AND fs.starts_at <= NOW() AND fs.ends_at > NOW() " +
                "ORDER BY fs.starts_at ASC"
            );
            List<FlashSaleTeaserResponse> result = new ArrayList<>();
            for (var sale : sales) {
                String saleId = sale.get("id").toString();
                var items = jdbcTemplate.queryForList(
                    "SELECT fsi.id, fsi.medicine_name, fsi.original_price, fsi.sale_price, " +
                    "fsi.image_url, fsi.qty_limit, fsi.sold_qty " +
                    "FROM pcms_ecom_ops.flash_sale_items fsi " +
                    "WHERE fsi.flash_sale_id = ?", sale.get("id")
                );
                List<FlashSaleItemResponse> itemList = items.stream()
                    .map(i -> new FlashSaleItemResponse(
                        i.get("id").toString(),
                        i.get("medicine_name").toString(),
                        new BigDecimal(i.get("original_price").toString()),
                        new BigDecimal(i.get("sale_price").toString()),
                        i.get("image_url") != null ? i.get("image_url").toString() : "",
                        ((Number) i.get("qty_limit")).intValue(),
                        ((Number) i.get("sold_qty")).intValue()
                    ))
                    .toList();
                result.add(new FlashSaleTeaserResponse(
                    saleId,
                    sale.get("name").toString(),
                    sale.get("description") != null ? sale.get("description").toString() : "",
                    new BigDecimal(sale.get("discount_pct").toString()),
                    sale.get("starts_at").toString(),
                    sale.get("ends_at").toString(),
                    itemList
                ));
            }
            return result;
        } catch (Exception e) {
            log.warn("Failed to load flash sales: {}", e.getMessage());
            return List.of();
        }
    }

    /** Best-seller price helper for future use when wiring OrderClient */
    @SuppressWarnings("unused")
    private static BigDecimal zeroPrice() { return BigDecimal.ZERO; }

    /** Convert MySQL binary UUID (byte[16]) to UUID string. */
    private static String uuidBytesToString(Object raw) {
        if (raw instanceof byte[] bytes && bytes.length == 16) {
            ByteBuffer bb = ByteBuffer.wrap(bytes);
            long high = bb.getLong();
            long low = bb.getLong();
            return new UUID(high, low).toString();
        }
        return raw != null ? raw.toString() : null;
    }
}