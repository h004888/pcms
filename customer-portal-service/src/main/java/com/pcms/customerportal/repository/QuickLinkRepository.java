package com.pcms.customerportal.repository;

import com.pcms.customerportal.entity.QuickLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuickLinkRepository extends JpaRepository<QuickLink, UUID> {

    List<QuickLink> findByStatusOrderBySortOrderAsc(String status);
}
