package com.pcms.orderservice.repository;

import com.pcms.orderservice.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for OrderItem — used for best-sellers aggregation
 * and inventory analytics.
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    /**
     * Top N medicines by total quantity sold within a given period.
     * Only counts items from PAID orders.
     */
    @Query(value = """
        SELECT BIN_TO_UUID(oi.medicine_id) AS medicineId,
               oi.medicine_name AS medicineName,
               SUM(oi.qty) AS soldCount,
               oi.unit_price AS unitPrice
        FROM order_items oi
        JOIN orders o ON o.id = oi.order_id
        WHERE o.status IN ('PAID', 'APPROVED', 'COMPLETED')
          AND o.created_at >= :since
        GROUP BY oi.medicine_id, oi.medicine_name, oi.unit_price
        ORDER BY soldCount DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findTopMedicines(@Param("since") LocalDateTime since,
                                    @Param("limit") int limit);
}
