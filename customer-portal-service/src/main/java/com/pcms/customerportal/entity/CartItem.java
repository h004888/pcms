package com.pcms.customerportal.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Single line item in a B2C cart.
 *
 * <p>unit_price is captured at the moment the item is added (FR14.4 stable
 * price preview). The cart total is recomputed on every mutation so
 * catalog-service price changes after add still show the new total.
 *
 * <p>BR04 bulk discount (5% off a line when qty ≥ 10 of the same medicine)
 * is applied at the subtotal recompute stage, not stored here. Storage of
 * "discounted subtotal" would lose the ability to recompute later.
 */
@Entity
@Table(name = "cart_items", indexes = {
        @Index(name = "idx_cart_item_cart", columnList = "cart_id"),
        @Index(name = "idx_cart_item_medicine", columnList = "medicine_id")
}, uniqueConstraints = {
        // (cart_id, medicine_id) is not unique because customers may want
        // to add the same medicine twice with different notes — the API
        // merges duplicates instead. If future product requires "one row
        // per medicine", add the unique constraint here.
})
@EntityListeners(AuditingEntityListener.class)
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Owning cart. Back-reference managed via Cart.addItem / removeItem. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(name = "medicine_id", nullable = false)
    private UUID medicineId;

    /** Snapshot of medicine name for display in cart (denormalized). */
    @Column(name = "medicine_name", length = 200)
    private String medicineName;

    @Column(nullable = false)
    private Integer qty;

    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    /** Convenience: qty * unit_price (kept in sync by service layer). */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    /** Optional image URL snapshot for cart UI. */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private java.time.LocalDateTime updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }
    public UUID getMedicineId() { return medicineId; }
    public void setMedicineId(UUID medicineId) { this.medicineId = medicineId; }
    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
    public Integer getQty() { return qty; }
    public void setQty(Integer qty) { this.qty = qty; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}