package com.pcms.categoryservice.service.impl;

import com.pcms.categoryservice.client.CatalogClient;
import com.pcms.categoryservice.dto.request.CreateCategoryRequest;
import com.pcms.categoryservice.dto.request.UpdateCategoryRequest;
import com.pcms.categoryservice.dto.response.CategoryResponse;
import com.pcms.categoryservice.entity.Category;
import com.pcms.categoryservice.enums.CategoryStatus;
import com.pcms.categoryservice.repository.CategoryRepository;
import com.pcms.categoryservice.service.CategoryService;
import com.pcms.common.exception.DuplicateResourceException;
import com.pcms.common.exception.InvalidOperationException;
import com.pcms.common.exception.ResourceNotFoundException;
import feign.FeignException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    private final CatalogClient catalogClient;

    public CategoryServiceImpl(CategoryRepository repository, CatalogClient catalogClient) {
        this.repository = repository;
        this.catalogClient = catalogClient;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "categorySearch", key = "#search == null ? '' : #search.toLowerCase()")
    public Page<CategoryResponse> list(String search, Pageable pageable) {
        if (search == null || search.isBlank()) {
            List<CategoryResponse> active = repository.findByStatus(CategoryStatus.ACTIVE).stream()
                    .map(this::toResponse).toList();
            return page(active, pageable);
        }
        List<Category> matches = repository.findByNameContainingIgnoreCaseAndStatus(search, CategoryStatus.ACTIVE);
        List<CategoryResponse> mapped = matches.stream().map(this::toResponse).toList();
        return page(mapped, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getById(UUID id) {
        Category category = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        return toResponse(category);
    }

    @Override
    @CacheEvict(value = "categorySearch", allEntries = true)
    public CategoryResponse create(CreateCategoryRequest request) {
        if (repository.existsByName(request.name())) {
            throw new DuplicateResourceException("name", request.name());
        }
        Category category = new Category();
        category.setName(request.name());
        category.setDescription(request.description());
        category.setStatus(CategoryStatus.ACTIVE);
        return toResponse(repository.save(category));
    }

    @Override
    @CacheEvict(value = "categorySearch", allEntries = true)
    public CategoryResponse update(UUID id, UpdateCategoryRequest request) {
        Category category = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        if (!category.getName().equals(request.name()) && repository.existsByNameAndIdNot(request.name(), id)) {
            throw new DuplicateResourceException("name", request.name());
        }
        category.setName(request.name());
        category.setDescription(request.description());
        if (request.status() != null) {
            category.setStatus(request.status());
        }
        return toResponse(repository.save(category));
    }

    @Override
    @CacheEvict(value = "categorySearch", allEntries = true)
    public void delete(UUID id) {
        Category category = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        long medicineCount = countMedicines(id);
        if (medicineCount > 0) {
            throw new InvalidOperationException(
                    "Cannot delete category with medicines",
                    "Không thể xóa danh mục đang có " + medicineCount + " thuốc");
        }
        category.setStatus(CategoryStatus.INACTIVE);
        repository.save(category);
    }

    private Page<CategoryResponse> page(List<CategoryResponse> mapped, Pageable pageable) {
        int total = mapped.size();
        int start = (int) Math.min((long) pageable.getPageNumber() * pageable.getPageSize(), total);
        int end = Math.min(start + pageable.getPageSize(), total);
        return new PageImpl<>(mapped.subList(start, end), pageable, total);
    }

    private long countMedicines(UUID categoryId) {
        try {
            return catalogClient.countMedicinesByCategory(categoryId);
        } catch (FeignException ex) {
            throw new InvalidOperationException(
                    "Cannot validate medicines for category",
                    "Không thể kiểm tra thuốc thuộc danh mục này");
        }
    }

    private CategoryResponse toResponse(Category entity) {
        return new CategoryResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
