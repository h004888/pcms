package com.pcms.orderservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class OrderItemRequestTest {

    @Test
    void constructor_withUnitPrice_setsField() {
        UUID medicineId = UUID.randomUUID();
        int quantity = 2;
        BigDecimal unitPrice = BigDecimal.valueOf(99999);

        var request = new OrderItemRequest(medicineId, quantity, unitPrice);

        assertThat(request.unitPrice()).isEqualTo(unitPrice);
        assertThat(request.medicineId()).isEqualTo(medicineId);
        assertThat(request.quantity()).isEqualTo(quantity);
    }

    @Test
    void constructor_withoutUnitPrice_defaultsToNull() {
        UUID medicineId = UUID.randomUUID();
        int quantity = 2;

        var request = new OrderItemRequest(medicineId, quantity, null);

        assertThat(request.unitPrice()).isNull();
    }
}
