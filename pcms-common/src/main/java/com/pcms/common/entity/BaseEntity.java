package com.pcms.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Base entity with common audit fields (B-03).
 *
 * <p>All PCMS entities should extend this class to get:
 * <ul>
 *   <li>UUID primary key (auto-generated)</li>
 *   <li>{@code createdAt} / {@code updatedAt} audit timestamps</li>
 *   <li>Consistent {@code equals} / {@code hashCode} based on id</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>{@code
 * @Entity
 * @Table(name = "medicines")
 * public class Medicine extends BaseEntity {
 *     @Column(nullable = false)
 *     private String name;
 *     // ... no need to declare id, createdAt, updatedAt
 * }
 * }</pre>
 *
 * <p>Note: existing entities in the codebase have these fields declared
 * inline. New entities should use this base class. Migration can be
 * done incrementally per service.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseEntity that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
