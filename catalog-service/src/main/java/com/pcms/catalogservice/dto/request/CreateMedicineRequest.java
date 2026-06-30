package com.pcms.catalogservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateMedicineRequest(
                @Size(max = 20, message = "SKU không được vượt quá 20 ký tự") String sku,
                @NotBlank(message = "Tên thuốc không được để trống") @Size(max = 200, message = "Tên thuốc không được vượt quá 200 ký tự") String name,
                @NotNull(message = "Danh mục không được để trống") UUID categoryId,
                UUID supplierId,
                @NotNull(message = "Giá bán không được để trống") @DecimalMin(value = "0.01", message = "Giá bán phải lớn hơn 0") BigDecimal price,
                @NotBlank(message = "Đơn vị tính không được để trống") @Size(max = 20, message = "Đơn vị tính không được vượt quá 20 ký tự") String unit,
                Boolean prescriptionRequired,
                String imageUrl,
                String description,
                String usage,
                String ingredients) {
}
