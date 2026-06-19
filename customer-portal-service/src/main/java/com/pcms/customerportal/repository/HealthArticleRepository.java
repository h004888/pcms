package com.pcms.customerportal.repository;

import com.pcms.customerportal.entity.HealthArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HealthArticleRepository extends JpaRepository<HealthArticle, UUID> {
    Optional<HealthArticle> findBySlug(String slug);
    Page<HealthArticle> findByStatusOrderByPublishedAtDesc(String status, Pageable pageable);
    Page<HealthArticle> findByCategoryAndStatusOrderByPublishedAtDesc(String category, String status, Pageable pageable);
}