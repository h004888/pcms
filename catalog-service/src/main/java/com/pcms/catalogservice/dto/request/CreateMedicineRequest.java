package com.pcms.catalogservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateMedicineRequest(
        @NotBlank @Size(max = 20) String sku,
        @NotBlank @Size(max = 200) String name,
        @NotNull UUID categoryId,
        UUID supplierId,
        @NotNull @DecimalMin("0.01") BigDecimal price,
        @NotBlank @Size(max = 20) String unit,
        Boolean prescriptionRequired,
        String imageUrl
) {}
