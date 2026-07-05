package com.pcms.customerportal.repository;

import com.pcms.customerportal.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BrandRepository extends JpaRepository<Brand, UUID> {

    List<Brand> findByStatusOrderBySortOrderAsc(String status);
}
