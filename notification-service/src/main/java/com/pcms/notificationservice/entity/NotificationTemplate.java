package com.pcms.notificationservice.entity;

import com.pcms.notificationservice.enums.NotificationChannel;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * B8: Notification templates with variable substitution.
 * Variables use {{name}} syntax, e.g. {{customer_name}}, {{order_id}}.
 */
@Entity
@Table(name = "notification_templates", uniqueConstraints = {
        @UniqueConstraint(name = "uk_notification_template_code_channel", columnNames = { "code", "channel" }),
        @UniqueConstraint(name = "uk_template_key", columnNames = "template_key")
})
@EntityListeners(AuditingEntityListener.class)
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 80)
    private String code;

    @Column(name = "template_key", length = 50)
    private String templateKey;   // e.g. NTPL-ORDER-PAID (B8 alias for code)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationChannel channel;

    @Column(name = "title_template", nullable = false, length = 200)
    private String titleTemplate;

    @Column(name = "body_template", nullable = false, columnDefinition = "TEXT")
    private String bodyTemplate;

    @Column(columnDefinition = "TEXT")
    private String variables;

    @Column(nullable = false)
    private Boolean active = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public NotificationTemplate() {
    }

    public NotificationTemplate(String code, NotificationChannel channel, String titleTemplate,
            String bodyTemplate, String variables) {
        this.code = code;
        this.channel = channel;
        this.titleTemplate = titleTemplate;
        this.bodyTemplate = bodyTemplate;
        this.variables = variables;
        this.active = true;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getTemplateKey() { return templateKey; }
    public void setTemplateKey(String templateKey) { this.templateKey = templateKey; }
    public NotificationChannel getChannel() { return channel; }
    public void setChannel(NotificationChannel channel) { this.channel = channel; }
    public String getTitleTemplate() { return titleTemplate; }
    public void setTitleTemplate(String titleTemplate) { this.titleTemplate = titleTemplate; }
    public String getBodyTemplate() { return bodyTemplate; }
    public void setBodyTemplate(String bodyTemplate) { this.bodyTemplate = bodyTemplate; }
    public String getVariables() { return variables; }
    public void setVariables(String variables) { this.variables = variables; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
