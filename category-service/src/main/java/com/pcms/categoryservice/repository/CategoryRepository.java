package com.pcms.categoryservice.repository;

import com.pcms.categoryservice.entity.Category;
import com.pcms.categoryservice.enums.CategoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Optional<Category> findByName(String name);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, UUID id);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    Optional<Category> findBySlug(String slug);

    List<Category> findByStatus(CategoryStatus status);

    List<Category> findByNameContainingIgnoreCase(String name);

    List<Category> findByNameContainingIgnoreCaseAndStatus(String name, CategoryStatus status);
}
