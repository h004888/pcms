package com.pcms.customerportal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Vaccine booking code sequence (mirrors OrderSequence in order-service).
 *
 * <p>Each row holds the last sequence number used for a given date prefix
 * (yyyyMMdd). The row is locked with {@code PESSIMISTIC_WRITE} in
 * {@code VaccineBookingSequenceRepository#findByIdForUpdate} to serialize
 * concurrent booking code generation.
 */
@Entity
@Table(name = "vaccine_booking_sequence")
public class VaccineBookingSequence {

    @Id
    @Column(name = "date_prefix", length = 8, nullable = false)
    private String datePrefix;

    @Column(name = "last_seq", nullable = false)
    private int lastSeq;

    public VaccineBookingSequence() {}

    public VaccineBookingSequence(String datePrefix, int lastSeq) {
        this.datePrefix = datePrefix;
        this.lastSeq = lastSeq;
    }

    public String getDatePrefix() { return datePrefix; }
    public void setDatePrefix(String datePrefix) { this.datePrefix = datePrefix; }
    public int getLastSeq() { return lastSeq; }
    public void setLastSeq(int lastSeq) { this.lastSeq = lastSeq; }
}