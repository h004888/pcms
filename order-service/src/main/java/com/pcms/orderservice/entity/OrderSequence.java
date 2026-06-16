package com.pcms.orderservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Order number sequence (B-08).
 * <p>Each row holds the last sequence number used for a given date prefix.
 * Locked with {@code SELECT ... FOR UPDATE} in the repository to serialize
 * concurrent order number generation.
 */
@Entity
@Table(name = "order_sequence")
public class OrderSequence {

    @Id
    @Column(name = "date_prefix", length = 8, nullable = false)
    private String datePrefix;  // yyyyMMdd

    @Column(name = "last_seq", nullable = false)
    private int lastSeq;

    public OrderSequence() {}

    public OrderSequence(String datePrefix, int lastSeq) {
        this.datePrefix = datePrefix;
        this.lastSeq = lastSeq;
    }

    public String getDatePrefix() { return datePrefix; }
    public void setDatePrefix(String datePrefix) { this.datePrefix = datePrefix; }
    public int getLastSeq() { return lastSeq; }
    public void setLastSeq(int lastSeq) { this.lastSeq = lastSeq; }
}
