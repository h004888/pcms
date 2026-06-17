package com.pcms.notificationservice.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * B8: Notification templates with variable substitution.
 * Variables use {{name}} syntax, e.g. {{customer_name}}, {{order_id}}.
 */
@Entity
@Table(name = "notification_templates",
        uniqueConstraints = @UniqueConstraint(name = "uk_template_key", columnNames = "template_key"))
@EntityListeners(AuditingEntityListener.class)
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "template_key", nullable = false, length = 50)
    private String templateKey;   // e.g. NTPL-ORDER-PAID

    @Column(nullable = false, length = 150)
    private String titleTemplate;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String bodyTemplate;

    @Column(length = 20)
    private String channel;       // IN_APP | EMAIL | SMS | null (all)

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public NotificationTemplate() {}

    public UUID getId() { return id; }
    public String getTemplateKey() { return templateKey; }
    public void setTemplateKey(String templateKey) { this.templateKey = templateKey; }
    public String getTitleTemplate() { return titleTemplate; }
    public void setTitleTemplate(String titleTemplate) { this.titleTemplate = titleTemplate; }
    public String getBodyTemplate() { return bodyTemplate; }
    public void setBodyTemplate(String bodyTemplate) { this.bodyTemplate = bodyTemplate; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
