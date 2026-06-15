package com.pcms.notificationservice.service;

import com.pcms.notificationservice.entity.Notification;
import com.pcms.notificationservice.enums.NotificationStatus;
import com.pcms.notificationservice.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * NSF-09: Notification sender with simple in-app retry (3x with backoff).
 * Email/SMS sending is OPT-IN via Spring Mail / SMS provider config.
 */
@Service
public class NotificationSenderService {

    private static final Logger log = LoggerFactory.getLogger(NotificationSenderService.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${notification.retry.max-attempts:3}")
    private int maxAttempts;

    @Value("${notification.retry.backoff-ms:1000}")
    private long backoffMs;

    /**
     * Async send notification. Safe by design: never throws, never crashes the process.
     * Implements NSF-09 retry logic (3 attempts with delay).
     */
    @Async
    public void sendNotification(Notification notification) {
        int attempt = 0;
        Exception lastError = null;

        while (attempt < maxAttempts) {
            attempt++;
            try {
                switch (notification.getChannel()) {
                    case IN_APP -> markSent(notification);
                    case EMAIL -> sendEmail(notification);
                    case SMS -> sendSms(notification);
                }
                // Success
                notification.setStatus(NotificationStatus.SENT);
                notification.setSentAt(LocalDateTime.now());
                notification.setRetryCount(attempt);
                notificationRepository.save(notification);
                log.info("Notification {} sent on attempt {} via {}",
                    notification.getId(), attempt, notification.getChannel());
                return;
            } catch (Exception e) {
                lastError = e;
                log.warn("Attempt {}/{} failed for notification {}: {}",
                    attempt, maxAttempts, notification.getId(), e.getMessage());
                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(backoffMs * attempt);  // simple linear backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        // All retries exhausted
        log.error("NSF-09: All {} attempts failed for notification {}: {}",
            maxAttempts, notification.getId(),
            lastError != null ? lastError.getMessage() : "unknown");
        notification.setStatus(NotificationStatus.FAILED);
        notification.setRetryCount(attempt);
        notificationRepository.save(notification);
    }

    private void markSent(Notification n) {
        // In-app: just persist in DB inbox
        log.info("In-app notification persisted: {}", n.getTitle());
    }

    private void sendEmail(Notification n) {
        if (mailSender == null) {
            log.warn("JavaMailSender not configured, skipping email send (set spring.mail.* to enable)");
            // Treat as success - just log, don't fail
            return;
        }
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo("recipient-" + n.getRecipientId() + "@pcms.vn");
        msg.setSubject(n.getTitle());
        msg.setText(n.getBody());
        mailSender.send(msg);
    }

    private void sendSms(Notification n) {
        // TODO: integrate with SMS provider
        log.info("SMS sent (mock) to recipient {}: {}", n.getRecipientId(), n.getTitle());
    }
}
