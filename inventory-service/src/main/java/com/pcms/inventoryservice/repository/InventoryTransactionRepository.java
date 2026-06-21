package com.pcms.inventoryservice.repository;

import com.pcms.inventoryservice.entity.InventoryTransaction;
import com.pcms.inventoryservice.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, UUID> {

    List<InventoryTransaction> findByBatchId(UUID batchId);

    List<InventoryTransaction> findByType(TransactionType type);

    List<InventoryTransaction> findByRefId(UUID refId);

    List<InventoryTransaction> findByRefIdAndType(UUID refId, TransactionType type);
}
