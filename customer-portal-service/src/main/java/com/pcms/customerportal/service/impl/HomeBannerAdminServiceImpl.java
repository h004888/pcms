package com.pcms.customerportal.service.impl;

import com.pcms.common.dto.PageResponse;
import com.pcms.common.exception.ResourceNotFoundException;
import com.pcms.customerportal.dto.request.CreateHomeBannerRequest;
import com.pcms.customerportal.dto.request.UpdateHomeBannerRequest;
import com.pcms.customerportal.dto.response.HomeBannerAdminResponse;
import com.pcms.customerportal.entity.HomeBanner;
import com.pcms.customerportal.enums.BannerStatus;
import com.pcms.customerportal.repository.HomeBannerRepository;
import com.pcms.customerportal.service.HomeBannerAdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class HomeBannerAdminServiceImpl implements HomeBannerAdminService {

    private final HomeBannerRepository bannerRepo;

    public HomeBannerAdminServiceImpl(HomeBannerRepository bannerRepo) {
        this.bannerRepo = bannerRepo;
    }

    @Override
    public PageResponse<HomeBannerAdminResponse> list(BannerStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100),
                Sort.by(Sort.Direction.ASC, "sortOrder")
                        .and(Sort.by(Sort.Direction.DESC, "createdAt")));
        Page<HomeBanner> all = bannerRepo.findAll(pageable);
        List<HomeBanner> filtered = status == null
                ? all.getContent()
                : all.getContent().stream().filter(b -> b.getStatus() == status).toList();
        Page<HomeBanner> result = new PageImpl<>(filtered, pageable, filtered.size());
        return PageResponse.from(result.map(this::toDto));
    }

    @Override
    @Transactional
    public HomeBannerAdminResponse create(CreateHomeBannerRequest req) {
        HomeBanner b = new HomeBanner();
        b.setTitle(req.title());
        b.setImageUrl(req.imageUrl());
        b.setLinkUrl(req.linkUrl());
        b.setSortOrder(req.sortOrder() != null ? req.sortOrder() : 0);
        b.setStatus(req.status() != null ? req.status() : BannerStatus.ACTIVE);
        b.setStartAt(req.startAt());
        b.setEndAt(req.endAt());
        LocalDateTime now = LocalDateTime.now();
        b.setCreatedAt(now);
        b.setUpdatedAt(now);
        return toDto(bannerRepo.save(b));
    }

    @Override
    @Transactional
    public HomeBannerAdminResponse update(UUID id, UpdateHomeBannerRequest req) {
        HomeBanner b = bannerRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HomeBanner", id));
        if (req.title() != null)       b.setTitle(req.title());
        if (req.imageUrl() != null)    b.setImageUrl(req.imageUrl());
        if (req.linkUrl() != null)     b.setLinkUrl(req.linkUrl());
        if (req.sortOrder() != null)   b.setSortOrder(req.sortOrder());
        if (req.status() != null)       b.setStatus(req.status());
        if (req.startAt() != null)      b.setStartAt(req.startAt());
        if (req.endAt() != null)        b.setEndAt(req.endAt());
        b.setUpdatedAt(LocalDateTime.now());
        return toDto(bannerRepo.save(b));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        HomeBanner b = bannerRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HomeBanner", id));
        b.setStatus(BannerStatus.DELETED);
        b.setUpdatedAt(LocalDateTime.now());
        bannerRepo.save(b);
    }

    private HomeBannerAdminResponse toDto(HomeBanner b) {
        return new HomeBannerAdminResponse(
                b.getId(), b.getTitle(), b.getImageUrl(), b.getLinkUrl(),
                b.getSortOrder(), b.getStatus(),
                b.getStartAt(), b.getEndAt(),
                b.getCreatedAt(), b.getUpdatedAt()
        );
    }
}
