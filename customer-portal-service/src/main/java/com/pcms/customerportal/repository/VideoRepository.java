package com.pcms.customerportal.repository;

import com.pcms.customerportal.entity.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VideoRepository extends JpaRepository<Video, UUID> {

    Page<Video> findByStatusOrderByViewCountDesc(String status, Pageable pageable);

    List<Video> findTop6ByStatusOrderByCreatedAtDesc(String status);

    // Admin methods (VideoAdminService)
    Page<Video> findByStatus(String status, Pageable pageable);
    Page<Video> findByCategoryAndStatus(String category, String status, Pageable pageable);
}
