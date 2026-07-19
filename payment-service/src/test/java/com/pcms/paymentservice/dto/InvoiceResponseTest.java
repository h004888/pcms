package com.pcms.paymentservice.dto;

import com.pcms.paymentservice.entity.Payment;
import com.pcms.paymentservice.enums.PaymentMethod;
import com.pcms.paymentservice.enums.PaymentStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceResponseTest {

    @Test
    void minimal_withNullStaffId_doesNotThrowNpe() {
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setOrderId(UUID.randomUUID());
        payment.setInvoiceNumber("INV-20260719-0001");
        payment.setPaymentMethod(PaymentMethod.CASH);
        payment.setAmount(new BigDecimal("500000"));
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setStaffId(null);

        InvoiceResponse response = InvoiceResponse.minimal(payment);
        assertThat(response).isNotNull();
        assertThat(response.staffId()).isNull();
    }

    @Test
    void minimal_withNullStaffId_setsStaffNameToPlaceholder() {
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setOrderId(UUID.randomUUID());
        payment.setInvoiceNumber("INV-20260719-0002");
        payment.setPaymentMethod(PaymentMethod.CARD);
        payment.setAmount(new BigDecimal("300000"));
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setStaffId(null);

        InvoiceResponse response = InvoiceResponse.minimal(payment);
        assertThat(response).isNotNull();
        assertThat(response.staffName()).isEqualTo("Khách lẻ");
    }
}
