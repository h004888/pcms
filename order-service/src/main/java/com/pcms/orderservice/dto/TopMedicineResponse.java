package com.pcms.orderservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * SHOP-HOME best-sellers aggregation result.
 * Each record represents one medicine + its total sold quantity.
 */
public class TopMedicineResponse {

    private UUID medicineId;
    private String medicineName;
    private BigDecimal price;
    private Long soldCount;

    public TopMedicineResponse() {}

    public TopMedicineResponse(UUID medicineId, String medicineName, BigDecimal price, Long soldCount) {
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.price = price;
        this.soldCount = soldCount;
    }

    public UUID getMedicineId() { return medicineId; }
    public void setMedicineId(UUID medicineId) { this.medicineId = medicineId; }
    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Long getSoldCount() { return soldCount; }
    public void setSoldCount(Long soldCount) { this.soldCount = soldCount; }
}
