package com.pcms.customerportal.service;

import com.pcms.customerportal.dto.request.ApplyVoucherRequest;
import com.pcms.customerportal.dto.response.ApplyVoucherResponse;
import com.pcms.customerportal.dto.response.VoucherResponse;
import com.pcms.customerportal.dto.response.VoucherUsageResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface VoucherService {

    List<VoucherResponse> listActive();

    ApplyVoucherResponse apply(UUID customerId, ApplyVoucherRequest request, BigDecimal cartTotal);

    List<VoucherUsageResponse> history(UUID customerId);
}
