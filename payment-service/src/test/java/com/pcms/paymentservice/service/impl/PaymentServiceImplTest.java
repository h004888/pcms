package com.pcms.paymentservice.service.impl;

import com.pcms.common.exception.InvalidOperationException;
import com.pcms.common.exception.ResourceNotFoundException;
import com.pcms.paymentservice.client.OrderClient;
import com.pcms.paymentservice.dto.PaymentResponse;
import com.pcms.paymentservice.entity.Payment;
import com.pcms.paymentservice.enums.PaymentStatus;
import com.pcms.paymentservice.repository.OutboxEventRepository;
import com.pcms.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private OrderClient orderClient;
    @Mock private OutboxEventRepository outboxEventRepository;
    @InjectMocks private PaymentServiceImpl paymentService;

    private UUID paymentId;

    @BeforeEach
    void setUp() {
        paymentId = UUID.randomUUID();
    }

    @Test
    void cancelPending_setsStatusCancelled() {
        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setAmount(BigDecimal.valueOf(100000));

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentResponse result = paymentService.cancelPending(paymentId);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        verify(paymentRepository).save(payment);
        assertThat(result).isNotNull();
    }

    @Test
    void cancelPending_throwsWhenNotPending() {
        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setStatus(PaymentStatus.SUCCESS);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.cancelPending(paymentId))
                .isInstanceOf(InvalidOperationException.class);

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void cancelPending_throwsWhenNotFound() {
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.cancelPending(paymentId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(paymentRepository, never()).save(any(Payment.class));
    }
}
