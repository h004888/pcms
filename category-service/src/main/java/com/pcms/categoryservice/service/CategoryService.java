package com.pcms.categoryservice.service;

import com.pcms.categoryservice.dto.request.CreateCategoryRequest;
import com.pcms.categoryservice.dto.request.UpdateCategoryRequest;
import com.pcms.categoryservice.dto.response.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CategoryService {
    Page<CategoryResponse> list(String search, Pageable pageable);

    CategoryResponse getById(UUID id);

    CategoryResponse getBySlug(String slug);

    CategoryResponse create(CreateCategoryRequest request);

    CategoryResponse update(UUID id, UpdateCategoryRequest request);

    void delete(UUID id);

    int backfillSlugs();
}
