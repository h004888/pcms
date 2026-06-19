package com.pcms.customerportal.service;

import com.pcms.common.dto.PageResponse;
import com.pcms.customerportal.dto.response.OrderHistoryItemResponse;
import com.pcms.customerportal.dto.response.OrderTrackingResponse;

import java.util.UUID;

public interface OrderTrackingService {

    OrderTrackingResponse track(UUID orderId, UUID customerId);

    PageResponse<OrderHistoryItemResponse> history(UUID customerId, int page, int size);
}
