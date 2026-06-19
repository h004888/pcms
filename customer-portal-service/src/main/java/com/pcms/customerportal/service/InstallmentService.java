package com.pcms.customerportal.service;

import com.pcms.customerportal.dto.request.InstallmentConfirmRequest;
import com.pcms.customerportal.dto.request.InstallmentQuoteRequest;
import com.pcms.customerportal.dto.response.InstallmentConfirmResponse;
import com.pcms.customerportal.dto.response.InstallmentQuoteResponse;

public interface InstallmentService {

    InstallmentQuoteResponse quote(InstallmentQuoteRequest request);

    InstallmentConfirmResponse confirm(InstallmentConfirmRequest request);
}
