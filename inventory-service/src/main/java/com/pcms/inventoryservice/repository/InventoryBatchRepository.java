package com.pcms.inventoryservice.repository;

import com.pcms.inventoryservice.entity.InventoryBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryBatchRepository extends JpaRepository<InventoryBatch, UUID> {

    Optional<InventoryBatch> findByMedicineIdAndBranchIdAndBatchNo(UUID medicineId, UUID branchId, String batchNo);

    Optional<InventoryBatch> findByBarcodeIgnoreCase(String barcode);

    List<InventoryBatch> findByBranchId(UUID branchId);

    List<InventoryBatch> findByMedicineIdAndBranchId(UUID medicineId, UUID branchId);

    /**
     * NSF-05: FIFO batch picker - oldest expiry first, ignore zero/negative qty
     */
    @Query("SELECT b FROM InventoryBatch b " +
            "WHERE b.medicineId = :medicineId AND b.branchId = :branchId " +
            "AND b.qtyOnHand > 0 AND b.expiryDate > :today " +
            "ORDER BY b.expiryDate ASC")
    List<InventoryBatch> findAvailableBatchesFifo(@Param("medicineId") UUID medicineId,
            @Param("branchId") UUID branchId,
            @Param("today") LocalDate today);

    /**
     * BR02: Find batches with stock < minStockLevel
     */
    @Query("SELECT b FROM InventoryBatch b WHERE b.qtyOnHand < b.minStockLevel")
    List<InventoryBatch> findLowStockBatches();

    /**
     * NSF-03/BR03: Find batches expiring within N days
     */
    @Query("SELECT b FROM InventoryBatch b WHERE b.expiryDate BETWEEN :today AND :alertDate")
    List<InventoryBatch> findExpiringBatches(@Param("today") LocalDate today,
            @Param("alertDate") LocalDate alertDate);

    /** Total stock across all branches for a medicine */
    @Query("SELECT COALESCE(SUM(b.qtyOnHand), 0) FROM InventoryBatch b WHERE b.medicineId = :medicineId")
    Long totalStockForMedicine(@Param("medicineId") UUID medicineId);
}
