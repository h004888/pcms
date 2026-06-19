package com.pcms.customerportal.service;

import com.pcms.customerportal.dto.request.CheckoutConfirmRequest;
import com.pcms.customerportal.dto.request.CheckoutPreviewRequest;
import com.pcms.customerportal.dto.response.CheckoutConfirmResponse;
import com.pcms.customerportal.dto.response.CheckoutPreviewResponse;

import java.util.UUID;

public interface CheckoutService {

    CheckoutPreviewResponse preview(UUID customerId, CheckoutPreviewRequest request);

    CheckoutConfirmResponse confirm(UUID customerId, CheckoutConfirmRequest request);
}
