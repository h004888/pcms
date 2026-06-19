package com.pcms.ecomops.entity;

import com.pcms.common.entity.BaseEntity;
import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@Entity
@Table(name = "flash_sale_items", indexes = {
        @Index(name = "idx_flash_item_sale", columnList = "flash_sale_id")
})
@EntityListeners(AuditingEntityListener.class)
public class FlashSaleItem extends BaseEntity {

    @Column(name = "flash_sale_id", nullable = false)
    private UUID flashSaleId;

    @Column(name = "medicine_id", nullable = false)
    private UUID medicineId;

    @Column(name = "original_price", nullable = false, precision = 15, scale = 2)
    private java.math.BigDecimal originalPrice;

    @Column(name = "sale_price", nullable = false, precision = 15, scale = 2)
    private java.math.BigDecimal salePrice;

    @Column(name = "qty_limit", nullable = false)
    private Integer qtyLimit;

    @Column(name = "sold_qty", nullable = false)
    private Integer soldQty = 0;

    public UUID getFlashSaleId() { return flashSaleId; }
    public void setFlashSaleId(UUID flashSaleId) { this.flashSaleId = flashSaleId; }
    public UUID getMedicineId() { return medicineId; }
    public void setMedicineId(UUID medicineId) { this.medicineId = medicineId; }
    public java.math.BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(java.math.BigDecimal originalPrice) { this.originalPrice = originalPrice; }
    public java.math.BigDecimal getSalePrice() { return salePrice; }
    public void setSalePrice(java.math.BigDecimal salePrice) { this.salePrice = salePrice; }
    public Integer getQtyLimit() { return qtyLimit; }
    public void setQtyLimit(Integer qtyLimit) { this.qtyLimit = qtyLimit; }
    public Integer getSoldQty() { return soldQty; }
    public void setSoldQty(Integer soldQty) { this.soldQty = soldQty; }
}
