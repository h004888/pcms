package com.pcms.customerportal.repository;

import com.pcms.customerportal.entity.HomeBanner;
import com.pcms.customerportal.enums.BannerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface HomeBannerRepository extends JpaRepository<HomeBanner, UUID> {

    /**
     * Active banners whose schedule window is in effect.
     * Used for SHOP-HOME hero carousel.
     */
    @Query("""
            SELECT b FROM HomeBanner b
            WHERE b.status = :status
              AND (b.startAt IS NULL OR b.startAt <= :now)
              AND (b.endAt   IS NULL OR b.endAt   >= :now)
            ORDER BY b.sortOrder ASC, b.createdAt DESC
            """)
    List<HomeBanner> findVisible(@Param("status") BannerStatus status,
                                 @Param("now") LocalDateTime now);
}
