package com.pcms.branchservice.repository;

import com.pcms.branchservice.entity.Branch;
import com.pcms.branchservice.enums.BranchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BranchRepository extends JpaRepository<Branch, UUID> {

    Optional<Branch> findByCode(String code);

    boolean existsByCode(String code);

    List<Branch> findByStatus(BranchStatus status);

    List<Branch> findByManagerId(UUID managerId);

    @Query("SELECT b FROM Branch b WHERE " +
           "(:search IS NULL OR LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "  OR LOWER(b.code) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:province IS NULL OR LOWER(b.province) LIKE LOWER(CONCAT('%', :province, '%'))) AND " +
           "(:district IS NULL OR LOWER(b.district) LIKE LOWER(CONCAT('%', :district, '%')))")
    Page<Branch> searchBranches(@Param("search") String search,
                                 @Param("province") String province,
                                 @Param("district") String district,
                                 Pageable pageable);
}
