package com.pcms.orderservice.repository;

import com.pcms.orderservice.entity.Order;
import com.pcms.orderservice.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    Page<Order> findByCustomerId(UUID customerId, Pageable pageable);

    List<Order> findByBranchId(UUID branchId);

    @Query("SELECT o FROM Order o WHERE "
            + "(:customerId IS NULL OR o.customerId = :customerId) "
            + "AND (:status IS NULL OR o.status = :status) "
            + "AND (:branchId IS NULL OR o.branchId = :branchId) "
            + "AND (:fromDateTime IS NULL OR o.createdAt >= :fromDateTime) "
            + "AND (:toDateTime IS NULL OR o.createdAt <= :toDateTime)")
    Page<Order> search(
            @Param("customerId") UUID customerId,
            @Param("status") OrderStatus status,
            @Param("branchId") UUID branchId,
            @Param("fromDateTime") LocalDateTime fromDateTime,
            @Param("toDateTime") LocalDateTime toDateTime,
            Pageable pageable);

    /**
     * NSF-01: Find orders pending payment longer than N hours
     */
    @Query("SELECT o FROM Order o WHERE o.status = 'PENDING_PAYMENT' AND o.createdAt < :cutoff")
    List<Order> findStalePendingOrders(LocalDateTime cutoff);

    /**
     * NSF-12: Generate order number ORD-yyyymmdd-####
     */
    @Query("SELECT o FROM Order o WHERE o.orderNumber LIKE CONCAT('ORD-', :datePrefix, '%') ORDER BY o.orderNumber DESC")
    List<Order> findByDatePrefix(String datePrefix, Pageable pageable);
}
