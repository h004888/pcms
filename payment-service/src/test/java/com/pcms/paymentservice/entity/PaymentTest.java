package com.pcms.paymentservice.entity;

import com.pcms.paymentservice.enums.PaymentMethod;
import com.pcms.paymentservice.enums.PaymentStatus;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void createPayment_withNullStaffId_isValid() {
        Payment payment = new Payment();
        payment.setOrderId(UUID.randomUUID());
        payment.setInvoiceNumber("INV-20260719-0001");
        payment.setPaymentMethod(PaymentMethod.CASH);
        payment.setAmount(new BigDecimal("500000"));
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setStaffId(null);

        var violations = validator.validate(payment);
        assertThat(violations).isEmpty();
    }
}
