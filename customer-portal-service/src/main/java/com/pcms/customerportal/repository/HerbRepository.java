package com.pcms.customerportal.repository;

import com.pcms.customerportal.entity.Herb;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HerbRepository extends JpaRepository<Herb, UUID> {

    @Query("""
            SELECT h FROM Herb h
            WHERE (:q IS NULL OR :q = ''
                   OR LOWER(h.nameVi) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(h.nameEn) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY h.nameVi ASC
            """)
    Page<Herb> searchByName(@Param("q") String q, Pageable pageable);
}