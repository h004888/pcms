package com.pcms.categoryservice.service.impl;

import com.pcms.categoryservice.dto.request.CreateCategoryRequest;
import com.pcms.categoryservice.dto.request.UpdateCategoryRequest;
import com.pcms.categoryservice.dto.response.CategoryResponse;
import com.pcms.categoryservice.entity.Category;
import com.pcms.categoryservice.repository.CategoryRepository;
import com.pcms.categoryservice.service.CategoryService;
import com.pcms.common.exception.DuplicateResourceException;
import com.pcms.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;

    public CategoryServiceImpl(CategoryRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> list(String search, Pageable pageable) {
        if (search == null || search.isBlank()) {
            return repository.findAll(pageable).map(this::toResponse);
        }
        List<Category> matches = repository.findByNameContainingIgnoreCase(search);
        List<CategoryResponse> mapped = matches.stream().map(this::toResponse).toList();
        // Manual page slice to keep existing search semantics
        int total = mapped.size();
        int start = (int) Math.min((long) pageable.getPageNumber() * pageable.getPageSize(), total);
        int end = Math.min(start + pageable.getPageSize(), total);
        return new PageImpl<>(mapped.subList(start, end), pageable, total);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getById(UUID id) {
        Category category = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        return toResponse(category);
    }

    @Override
    public CategoryResponse create(CreateCategoryRequest request) {
        if (repository.existsByName(request.name())) {
            throw new DuplicateResourceException("name", request.name());
        }
        Category category = new Category();
        category.setName(request.name());
        category.setDescription(request.description());
        return toResponse(repository.save(category));
    }

    @Override
    public CategoryResponse update(UUID id, UpdateCategoryRequest request) {
        Category category = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        category.setName(request.name());
        category.setDescription(request.description());
        return toResponse(repository.save(category));
    }

    @Override
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Category", id);
        }
        repository.deleteById(id);
    }

    private CategoryResponse toResponse(Category entity) {
        return new CategoryResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getCreatedAt()
        );
    }
}
