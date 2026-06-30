package com.pcms.customerportal.repository;

import com.pcms.customerportal.entity.DiseaseInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DiseaseInfoRepository extends JpaRepository<DiseaseInfo, UUID> {
    Page<DiseaseInfo> findAll(Pageable pageable);
    Page<DiseaseInfo> findByTargetAudience(String audience, Pageable pageable);
    Page<DiseaseInfo> findBySeason(String season, Pageable pageable);
    Page<DiseaseInfo> findByTargetAudienceAndSeason(String audience, String season, Pageable pageable);

    // SPRINT 1 - T07: lookup by slug for /diseases/{slug}
    java.util.Optional<DiseaseInfo> findBySlug(String slug);
}