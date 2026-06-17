package com.pcms.notificationservice.service.impl;

import com.pcms.common.exception.InvalidOperationException;
import com.pcms.common.exception.ResourceNotFoundException;
import com.pcms.notificationservice.dto.BroadcastRequest;
import com.pcms.notificationservice.dto.CreateNotificationRequest;
import com.pcms.notificationservice.dto.NotificationResponse;
import com.pcms.common.dto.PageResponse;
import com.pcms.notificationservice.entity.Notification;
import com.pcms.notificationservice.enums.NotificationChannel;
import com.pcms.notificationservice.enums.NotificationStatus;
import com.pcms.notificationservice.repository.NotificationRepository;
import com.pcms.notificationservice.service.NotificationSenderService;
import com.pcms.notificationservice.service.SmsSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of {@link NotificationSenderService}.
 * <p>
 * NSF-09: Notifications are sent asynchronously with up to 3 retries and
 * linear backoff. IN_APP notifications are simply persisted; EMAIL requires
 * an optional {@link JavaMailSender}; SMS is a mock placeholder.
 */
@Service
@Transactional
public class NotificationSenderServiceImpl implements NotificationSenderService {

    private static final Logger log = LoggerFactory.getLogger(NotificationSenderServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender; // may be null when mail is not configured
    private final int maxAttempts;
    private final long backoffMs;

    @Autowired(required = false)
    private SmsSender smsSender;

    public NotificationSenderServiceImpl(
            NotificationRepository notificationRepository,
            @org.springframework.beans.factory.annotation.Autowired(required = false) JavaMailSender mailSender,
            @Value("${notification.retry.max-attempts:3}") int maxAttempts,
            @Value("${notification.retry.backoff-ms:1000}") long backoffMs) {
        this.notificationRepository = notificationRepository;
        this.mailSender = mailSender;
        this.maxAttempts = maxAttempts;
        this.backoffMs = backoffMs;
    }

    @Override
    @Async
    public void send(Notification notification) {
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
                        Thread.sleep(backoffMs * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        log.error("NSF-09: All {} attempts failed for notification {}: {}",
                maxAttempts, notification.getId(),
                lastError != null ? lastError.getMessage() : "unknown");
        notification.setStatus(NotificationStatus.FAILED);
        notification.setRetryCount(attempt);
        notificationRepository.save(notification);
    }

    @Override
    public NotificationResponse createAndSend(CreateNotificationRequest request) {
        if (request.recipientId() == null) {
            throw new InvalidOperationException(
                    "Recipient is required",
                    "Người nhận là bắt buộc");
        }
        Notification n = new Notification();
        n.setRecipientId(request.recipientId());
        n.setChannel(request.channel());
        n.setTemplate(request.template());
        n.setTitle(request.title());
        n.setBody(request.body());
        n.setStatus(NotificationStatus.PENDING);
        Notification saved = notificationRepository.save(n);
        send(saved);
        return toResponse(saved);
    }

    @Override
    public int broadcast(BroadcastRequest request) {
        List<UUID> explicitRecipients = request.resolveRecipients();
        List<UUID> recipients = new ArrayList<>(explicitRecipients);
        // When audience uses roles/branches but no explicit users, leave the
        // list empty (resolution to a downstream user-service is not part of
        // this service).
        if (recipients.isEmpty()) {
            log.warn("Broadcast skipped: no resolvable recipients");
            return 0;
        }
        int sent = 0;
        for (UUID recipientId : recipients) {
            for (NotificationChannel channel : request.channels()) {
                Notification n = new Notification();
                n.setRecipientId(recipientId);
                n.setChannel(channel);
                n.setTemplate(request.template());
                n.setTitle(request.title());
                n.setBody(request.body());
                n.setStatus(NotificationStatus.PENDING);
                Notification saved = notificationRepository.save(n);
                send(saved);
                sent++;
            }
        }
        return sent;
    }

    @Override
    public NotificationResponse retry(UUID id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", id));
        if (n.getStatus() != NotificationStatus.FAILED) {
            throw new InvalidOperationException(
                    "Only FAILED can be retried",
                    "Chỉ có thể thử lại thông báo ở trạng thái THẤT BẠI");
        }
        int currentRetries = n.getRetryCount() == null ? 0 : n.getRetryCount();
        if (currentRetries >= maxAttempts) {
            throw new InvalidOperationException(
                    "Notification has exhausted its retries",
                    "Thông báo đã hết số lần thử lại",
                    409);
        }
        n.setStatus(NotificationStatus.PENDING);
        Notification saved = notificationRepository.save(n);
        send(saved);
        return toResponse(saved);
    }

    @Override
    public NotificationResponse markAsRead(UUID id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", id));
        if (n.getStatus() == NotificationStatus.READ) {
            return toResponse(n);
        }
        n.setStatus(NotificationStatus.READ);
        n.setReadAt(LocalDateTime.now());
        Notification saved = notificationRepository.save(n);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> list(UUID recipientId, int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<Notification> result = notificationRepository.findByRecipientId(recipientId, pageable);
        List<NotificationResponse> mapped = result.getContent().stream()
                .map(NotificationSenderServiceImpl::toResponse)
                .toList();
        return PageResponse.of(result, NotificationSenderServiceImpl::toResponse);
    }

    private void markSent(Notification n) {
        log.info("In-app notification persisted: {}", n.getTitle());
    }

    private void sendEmail(Notification n) {
        if (mailSender == null) {
            log.warn("JavaMailSender not configured, skipping email send (set spring.mail.* to enable)");
            return;
        }
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo("recipient-" + n.getRecipientId() + "@pcms.vn");
        msg.setSubject(n.getTitle());
        msg.setText(n.getBody());
        mailSender.send(msg);
    }

    private void sendSms(Notification n) {
        if (smsSender != null) {
            smsSender.send("RECIPIENT-" + n.getRecipientId(), n.getTitle() + ": " + n.getBody());
        } else {
            log.info("[SMS-MOCK] recipient={} msg={}", n.getRecipientId(), n.getTitle());
        }
    }

    private static NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getRecipientId(),
                n.getChannel(),
                n.getTemplate(),
                n.getTitle(),
                n.getBody(),
                n.getStatus(),
                n.getSentAt(),
                n.getReadAt(),
                n.getCreatedAt()
        );
    }
}
