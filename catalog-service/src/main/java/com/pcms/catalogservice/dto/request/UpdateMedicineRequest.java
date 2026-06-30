package com.pcms.catalogservice.dto.request;

import com.pcms.catalogservice.enums.MedicineStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateMedicineRequest(
                @Size(max = 200, message = "Tên thuốc không được vượt quá 200 ký tự") String name,
                UUID categoryId,
                UUID supplierId,
                @DecimalMin(value = "0.01", message = "Giá bán phải lớn hơn 0") BigDecimal price,
                @Size(max = 20, message = "Đơn vị tính không được vượt quá 20 ký tự") String unit,
                Boolean prescriptionRequired,
                String imageUrl,
                String description,
                String usage,
                String ingredients,
                MedicineStatus status) {
}
