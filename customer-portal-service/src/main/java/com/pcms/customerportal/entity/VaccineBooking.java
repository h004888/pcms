package com.pcms.customerportal.entity;

import com.pcms.customerportal.enums.VaccineBookingStatus;
import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

/**
 * UC14 - VACCINE-LEDGER. Customer's vaccine appointment reservation.
 *
 * <p>The booking is the link between customer (or family member) and a
 * specific {@link VaccineSlot}. The ledger record is created when the
 * injection is actually administered (status = COMPLETED) by branch staff.
 */
@Entity
@Table(name = "vaccine_bookings", uniqueConstraints = {
        @UniqueConstraint(name = "uk_booking_code", columnNames = "booking_code")
}, indexes = {
        @Index(name = "idx_booking_customer", columnList = "customer_id"),
        @Index(name = "idx_booking_slot", columnList = "slot_id"),
        @Index(name = "idx_booking_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
public class VaccineBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Human-readable booking code: VC-yyyyMMdd-#### */
    @Column(name = "booking_code", nullable = false, length = 30)
    private String bookingCode;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "vaccine_id", nullable = false)
    private UUID vaccineId;

    @Column(name = "slot_id", nullable = false)
    private UUID slotId;

    /** Optional family member receiving the injection (Tài khoản gia đình, FR14.22). */
    @Column(name = "family_member_id")
    private UUID familyMemberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VaccineBookingStatus status = VaccineBookingStatus.BOOKED;

    @Column(name = "cancelled_at")
    private java.time.LocalDateTime cancelledAt;

    @Column(name = "cancel_reason", length = 255)
    private String cancelReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private java.time.LocalDateTime updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getBookingCode() { return bookingCode; }
    public void setBookingCode(String bookingCode) { this.bookingCode = bookingCode; }
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public UUID getVaccineId() { return vaccineId; }
    public void setVaccineId(UUID vaccineId) { this.vaccineId = vaccineId; }
    public UUID getSlotId() { return slotId; }
    public void setSlotId(UUID slotId) { this.slotId = slotId; }
    public UUID getFamilyMemberId() { return familyMemberId; }
    public void setFamilyMemberId(UUID familyMemberId) { this.familyMemberId = familyMemberId; }
    public VaccineBookingStatus getStatus() { return status; }
    public void setStatus(VaccineBookingStatus status) { this.status = status; }
    public java.time.LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(java.time.LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}