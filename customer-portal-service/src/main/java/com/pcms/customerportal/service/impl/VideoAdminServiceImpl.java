package com.pcms.customerportal.service.impl;

import com.pcms.common.dto.PageResponse;
import com.pcms.common.exception.InvalidOperationException;
import com.pcms.common.exception.ResourceNotFoundException;
import com.pcms.customerportal.dto.request.CreateVideoRequest;
import com.pcms.customerportal.dto.request.UpdateVideoRequest;
import com.pcms.customerportal.dto.response.VideoResponse;
import com.pcms.customerportal.entity.Video;
import com.pcms.customerportal.repository.VideoRepository;
import com.pcms.customerportal.service.VideoAdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class VideoAdminServiceImpl implements VideoAdminService {

    private final VideoRepository videoRepository;

    public VideoAdminServiceImpl(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<VideoResponse> listAll(String category, String status, int page, int size) {
        Page<Video> p;
        if (category != null && !category.isBlank()) {
            p = videoRepository.findByCategoryAndStatus(category, status != null ? status : "ACTIVE", PageRequest.of(page, size));
        } else if (status != null) {
            p = videoRepository.findByStatus(status, PageRequest.of(page, size));
        } else {
            p = videoRepository.findAll(PageRequest.of(page, size));
        }
        return PageResponse.of(p, this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoResponse> listActive() {
        return videoRepository.findTop6ByStatusOrderByCreatedAtDesc("ACTIVE")
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VideoResponse get(UUID id) {
        Video v = videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video", id));
        return toResponse(v);
    }

    @Override
    @Transactional
    public VideoResponse create(CreateVideoRequest request) {
        Video v = new Video();
        v.setTitle(request.title());
        v.setYoutubeId(request.youtubeId());
        v.setThumbnailUrl(request.thumbnailUrl());
        v.setSource(request.source());
        v.setDurationSec(request.durationSec());
        v.setCategory(request.category());
        v.setStatus("ACTIVE");
        v.setViewCount(0L);
        return toResponse(videoRepository.save(v));
    }

    @Override
    @Transactional
    public VideoResponse update(UUID id, UpdateVideoRequest request) {
        Video v = videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video", id));
        if (request.title() != null) v.setTitle(request.title());
        if (request.youtubeId() != null) v.setYoutubeId(request.youtubeId());
        if (request.thumbnailUrl() != null) v.setThumbnailUrl(request.thumbnailUrl());
        if (request.source() != null) v.setSource(request.source());
        if (request.durationSec() != null) v.setDurationSec(request.durationSec());
        if (request.category() != null) v.setCategory(request.category());
        if (request.status() != null) v.setStatus(request.status());
        return toResponse(videoRepository.save(v));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        Video v = videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video", id));
        if ("DELETED".equals(v.getStatus())) {
            throw new InvalidOperationException("Video already deleted", "Video đã xoá");
        }
        v.setStatus("DELETED");
        videoRepository.save(v);
    }

    private VideoResponse toResponse(Video v) {
        return new VideoResponse(
                v.getId(), v.getTitle(), v.getYoutubeId(), v.getThumbnailUrl(),
                v.getSource(), v.getDurationSec(), v.getCategory(),
                v.getViewCount(), v.getStatus()
        );
    }
}