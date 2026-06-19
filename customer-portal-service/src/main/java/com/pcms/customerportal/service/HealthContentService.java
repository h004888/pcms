package com.pcms.customerportal.service;

import com.pcms.common.dto.PageResponse;
import com.pcms.customerportal.dto.response.DiseaseInfoResponse;
import com.pcms.customerportal.dto.response.HealthArticleResponse;
import com.pcms.customerportal.dto.response.VerifyOriginResponse;
import com.pcms.customerportal.dto.request.ScanCodeRequest;

public interface HealthContentService {
    PageResponse<HealthArticleResponse> listArticles(String category, int page, int size);
    HealthArticleResponse getArticleBySlug(String slug);
    PageResponse<DiseaseInfoResponse> listDiseases(String audience, String season, int page, int size);
    VerifyOriginResponse verifyOrigin(ScanCodeRequest request);
}