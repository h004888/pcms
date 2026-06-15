package com.pcms.catalogservice.repository;

import com.pcms.catalogservice.entity.Medicine;
import com.pcms.catalogservice.enums.MedicineStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, UUID> {

    Optional<Medicine> findBySku(String sku);
    boolean existsBySku(String sku);

    List<Medicine> findByCategoryId(UUID categoryId);
    List<Medicine> findByStatus(MedicineStatus status);

    /** UC10: Search/filter medicines */
    @Query("SELECT m FROM Medicine m WHERE " +
           "(:query IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:categoryId IS NULL OR m.categoryId = :categoryId) " +
           "AND (:minPrice IS NULL OR m.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR m.price <= :maxPrice) " +
           "AND (:status IS NULL OR m.status = :status)")
    Page<Medicine> search(@Param("query") String query,
                          @Param("categoryId") UUID categoryId,
                          @Param("minPrice") BigDecimal minPrice,
                          @Param("maxPrice") BigDecimal maxPrice,
                          @Param("status") MedicineStatus status,
                          Pageable pageable);

    /** Autocomplete for UC10 AT2 */
    @Query("SELECT m FROM Medicine m WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :prefix, '%')) AND m.status = 'ACTIVE' ORDER BY m.name ASC")
    List<Medicine> findTop5ByNameLike(@Param("prefix") String prefix, Pageable pageable);
}
