package com.pcms.customerservice.dto.response;

import com.pcms.common.dto.PageResponse;

public record CustomerHistoryResponse(
        CustomerResponse customer,
        PageResponse<CustomerOrderSummaryResponse> orders,
        PageResponse<LoyaltyTransactionResponse> points) {
}