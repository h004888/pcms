package com.pcms.paymentservice.repository;

import com.pcms.paymentservice.entity.Payment;
import com.pcms.paymentservice.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByInvoiceNumber(String invoiceNumber);
    Optional<Payment> findByOrderId(UUID orderId);
    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.invoiceNumber LIKE CONCAT('INV-', :datePrefix, '%') ORDER BY p.invoiceNumber DESC")
    List<Payment> findByDatePrefix(String datePrefix, Pageable pageable);
}
