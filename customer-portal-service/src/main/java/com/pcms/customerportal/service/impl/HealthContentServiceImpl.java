package com.pcms.customerportal.service.impl;

import com.pcms.common.dto.PageResponse;
import com.pcms.common.exception.ResourceNotFoundException;
import com.pcms.customerportal.client.CatalogClient;
import com.pcms.customerportal.dto.request.ScanCodeRequest;
import com.pcms.customerportal.dto.response.DiseaseInfoResponse;
import com.pcms.customerportal.dto.response.HealthArticleResponse;
import com.pcms.customerportal.dto.response.VerifyOriginResponse;
import com.pcms.customerportal.entity.BatchVerification;
import com.pcms.customerportal.entity.DiseaseInfo;
import com.pcms.customerportal.entity.HealthArticle;
import com.pcms.customerportal.repository.BatchVerificationRepository;
import com.pcms.customerportal.repository.DiseaseInfoRepository;
import com.pcms.customerportal.repository.HealthArticleRepository;
import com.pcms.customerportal.service.HealthContentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
public class HealthContentServiceImpl implements HealthContentService {

    private final HealthArticleRepository articleRepository;
    private final DiseaseInfoRepository diseaseRepository;
    private final BatchVerificationRepository batchRepository;
    private final CatalogClient catalogClient;

    public HealthContentServiceImpl(HealthArticleRepository articleRepository,
                                    DiseaseInfoRepository diseaseRepository,
                                    BatchVerificationRepository batchRepository,
                                    CatalogClient catalogClient) {
        this.articleRepository = articleRepository;
        this.diseaseRepository = diseaseRepository;
        this.batchRepository = batchRepository;
        this.catalogClient = catalogClient;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<HealthArticleResponse> listArticles(String category, int page, int size) {
        Page<HealthArticle> p;
        if (category != null && !category.isBlank()) {
            p = articleRepository.findByCategoryAndStatusOrderByPublishedAtDesc(category, "PUBLISHED", PageRequest.of(page, size));
        } else {
            p = articleRepository.findByStatusOrderByPublishedAtDesc("PUBLISHED", PageRequest.of(page, size));
        }
        return PageResponse.of(p, this::toArticleResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public HealthArticleResponse getArticleBySlug(String slug) {
        HealthArticle article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("HealthArticle", "slug=" + slug));
        // Increment view count
        article.setViewCount(article.getViewCount() + 1);
        articleRepository.save(article);
        return toArticleResponse(article);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DiseaseInfoResponse> listDiseases(String audience, String season, int page, int size) {
        Page<DiseaseInfo> p;
        if (audience != null && !audience.isBlank() && season != null && !season.isBlank()) {
            p = diseaseRepository.findByTargetAudienceAndSeason(audience, season, PageRequest.of(page, size));
        } else if (audience != null && !audience.isBlank()) {
            p = diseaseRepository.findByTargetAudience(audience, PageRequest.of(page, size));
        } else if (season != null && !season.isBlank()) {
            p = diseaseRepository.findBySeason(season, PageRequest.of(page, size));
        } else {
            p = diseaseRepository.findAll(PageRequest.of(page, size));
        }
        return PageResponse.of(p, this::toDiseaseResponse);
    }

    // SPRINT 1 - T07: detail by slug, 404 nếu không có
    @Override
    @Transactional(readOnly = true)
    public DiseaseInfoResponse getDiseaseBySlug(String slug) {
        DiseaseInfo d = diseaseRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("DiseaseInfo", "slug=" + slug));
        return toDiseaseResponse(d);
    }

    @Override
    @Transactional(readOnly = true)
    public VerifyOriginResponse verifyOrigin(ScanCodeRequest request) {
        // Decode QR code (assume code = batchNo, or JSON {batchNo: "..."})
        String batchNo = request.code();
        if (batchNo.startsWith("{")) {
            // Simple JSON parsing - try to extract batchNo
            int idx = batchNo.indexOf("batchNo");
            if (idx > -1) {
                int start = batchNo.indexOf(":", idx) + 1;
                int end = batchNo.indexOf(",", start);
                if (end == -1) end = batchNo.indexOf("}", start);
                batchNo = batchNo.substring(start, end).trim().replaceAll("[\"\\s]", "");
            }
        }

        Optional<BatchVerification> opt = batchRepository.findByBatchNo(batchNo);
        if (opt.isEmpty()) {
            return new VerifyOriginResponse(
                    false, "Không tìm thấy thông tin nguồn gốc cho mã: " + batchNo,
                    null, null, batchNo, null, null, null, "NOT_FOUND"
            );
        }

        BatchVerification batch = opt.get();

        // Try to fetch medicine name from catalog-service
        String medicineName = null;
        if (batch.getMedicineId() != null) {
            try {
                Map<String, Object> med = catalogClient.getById(batch.getMedicineId().toString());
                medicineName = (String) med.getOrDefault("name", null);
            } catch (Exception ignored) {
                // Graceful degradation - medicine name unavailable
            }
        }

        return new VerifyOriginResponse(
                true, null,
                batch.getMedicineId(), medicineName,
                batch.getBatchNo(), batch.getManufacturer(),
                batch.getManufacturedAt(), batch.getVerifiedAt(),
                batch.getStatus()
        );
    }

    private HealthArticleResponse toArticleResponse(HealthArticle a) {
        return new HealthArticleResponse(
                a.getId(), a.getTitle(), a.getSlug(), a.getCategory(),
                a.getAuthor(), a.getBodyMarkdown(), a.getPublishedAt(), a.getViewCount()
        );
    }

    private DiseaseInfoResponse toDiseaseResponse(DiseaseInfo d) {
        return new DiseaseInfoResponse(
                d.getId(), d.getName(), d.getSlug(),
                d.getTargetAudience(), d.getSeason(), d.getSeverity(),
                d.getBody(), d.getViewCount()
        );
    }
}