package com.pcms.customerportal.service;

import com.pcms.common.dto.PageResponse;
import com.pcms.customerportal.dto.request.CreateVideoRequest;
import com.pcms.customerportal.dto.request.UpdateVideoRequest;
import com.pcms.customerportal.dto.response.VideoResponse;
import com.pcms.customerportal.entity.Video;

import java.util.List;
import java.util.UUID;

public interface VideoAdminService {
    PageResponse<VideoResponse> listAll(String category, String status, int page, int size);
    List<VideoResponse> listActive();
    VideoResponse get(UUID id);
    VideoResponse create(CreateVideoRequest request);
    VideoResponse update(UUID id, UpdateVideoRequest request);
    void softDelete(UUID id);
}