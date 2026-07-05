package com.pcms.customerportal.service;

import com.pcms.common.dto.PageResponse;
import com.pcms.customerportal.dto.request.CreateHomeBannerRequest;
import com.pcms.customerportal.dto.request.UpdateHomeBannerRequest;
import com.pcms.customerportal.dto.response.HomeBannerAdminResponse;
import com.pcms.customerportal.enums.BannerStatus;

import java.util.UUID;

/**
 * Admin CRUD cho home_banners.
 * Customer-facing dùng ShopHomeService.loadHeroBanners() trực tiếp từ HomeBannerRepository.
 */
public interface HomeBannerAdminService {
    PageResponse<HomeBannerAdminResponse> list(BannerStatus status, int page, int size);
    HomeBannerAdminResponse create(CreateHomeBannerRequest req);
    HomeBannerAdminResponse update(UUID id, UpdateHomeBannerRequest req);
    void softDelete(UUID id);
}
