package com.pcms.userservice.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Revoked JWT JTI blacklist for logout and token invalidation.
 */
@Entity
@Table(name = "blacklisted_tokens", uniqueConstraints = {
        @UniqueConstraint(name = "uk_blacklisted_token_jti", columnNames = "jti")
}, indexes = {
        @Index(name = "idx_blacklisted_token_user", columnList = "user_id"),
        @Index(name = "idx_blacklisted_token_expires", columnList = "expires_at")
})
@EntityListeners(AuditingEntityListener.class)
public class TokenBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 64)
    private String jti;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "token_type", length = 20)
    private String tokenType;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(length = 100)
    private String reason;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public TokenBlacklist() {
    }

    public TokenBlacklist(String jti, UUID userId, String tokenType, LocalDateTime expiresAt, String reason) {
        this.jti = jti;
        this.userId = userId;
        this.tokenType = tokenType;
        this.expiresAt = expiresAt;
        this.reason = reason;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getJti() {
        return jti;
    }

    public void setJti(String jti) {
        this.jti = jti;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}