package com.pcms.catalogservice.dto.request;

import com.pcms.catalogservice.enums.MedicineStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateMedicineRequest(
        @Size(max = 200) String name,
        @DecimalMin("0.01") BigDecimal price,
        @Size(max = 20) String unit,
        Boolean prescriptionRequired,
        String imageUrl,
        MedicineStatus status
) {}
