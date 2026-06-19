package com.pcms.customerportal.service;

import com.pcms.common.dto.PageResponse;
import com.pcms.customerportal.dto.request.AddFavoriteRequest;
import com.pcms.customerportal.dto.response.FavoriteResponse;

import java.util.UUID;

/**
 * TICKET-704 (Favorites) - Sản phẩm yêu thích.
 * FR14.25.
 */
public interface FavoriteService {

    PageResponse<FavoriteResponse> list(UUID currentCustomerId, int page, int size);

    /** Idempotent add - returns existing record if already favorited. */
    FavoriteResponse add(UUID currentCustomerId, AddFavoriteRequest request);

    void remove(UUID currentCustomerId, UUID medicineId);

    boolean isFavorite(UUID currentCustomerId, UUID medicineId);
}
