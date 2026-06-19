package com.pcms.notificationservice.service.impl;

import com.pcms.common.exception.InvalidOperationException;
import com.pcms.common.exception.ResourceNotFoundException;
import com.pcms.common.dto.PageResponse;
import com.pcms.notificationservice.dto.request.BroadcastRequest;
import com.pcms.notificationservice.dto.request.ComposeNotificationRequest;
import com.pcms.notificationservice.dto.request.CreateNotificationRequest;
import com.pcms.notificationservice.dto.response.ResolvedTemplateResponse;
import com.pcms.notificationservice.dto.response.NotificationResponse;
import com.pcms.notificationservice.entity.Notification;
import com.pcms.notificationservice.enums.NotificationChannel;
import com.pcms.notificationservice.enums.NotificationStatus;
import com.pcms.notificationservice.repository.NotificationRepository;
import com.pcms.notificationservice.service.NotificationSenderService;
import com.pcms.notificationservice.service.SmsSender;
import com.pcms.notificationservice.service.TemplateResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final TemplateResolver templateResolver;
    private final SmsSender smsSender;
    private final JavaMailSender mailSender; // may be null when mail is not configured
    private final int maxAttempts;
    private final long backoffMs;

    public NotificationSenderServiceImpl(
            NotificationRepository notificationRepository,
            TemplateResolver templateResolver,
            SmsSender smsSender,
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${notification.retry.max-attempts:3}") int maxAttempts,
            @Value("${notification.retry.backoff-ms:1000}") long backoffMs) {
        this.notificationRepository = notificationRepository;
        this.templateResolver = templateResolver;
        this.smsSender = smsSender;
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.maxAttempts = maxAttempts;
        this.backoffMs = backoffMs;
    }

    @Override
    @Async("notificationTaskExecutor")
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
            } catch (MailException | DataAccessException | IllegalStateException e) {
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
    public int compose(ComposeNotificationRequest request) {
        Set<UUID> recipients = resolveComposeRecipients(request);
        Set<NotificationChannel> channels = resolveComposeChannels(request);
        if (recipients.isEmpty()) {
            throw new InvalidOperationException(
                    "At least one recipient is required",
                    "Phải chọn ít nhất một người nhận");
        }
        if (channels.isEmpty()) {
            throw new InvalidOperationException(
                    "At least one channel is required",
                    "Phải chọn ít nhất một kênh gửi");
        }

        int queued = 0;
        for (UUID recipientId : recipients) {
            for (NotificationChannel channel : channels) {
                ResolvedTemplateResponse resolved = templateResolver.resolve(
                        request.template(), channel, request.title(), request.body(), request.variables());
                createAndSend(new CreateNotificationRequest(
                        recipientId,
                        channel,
                        request.template(),
                        resolved.title(),
                        resolved.body(),
                        null));
                queued++;
            }
        }
        return queued;
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
    @Transactional(readOnly = true)
    public NotificationResponse getById(UUID id) {
        return notificationRepository.findById(id)
                .map(NotificationSenderServiceImpl::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", id));
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
        return list(recipientId, null, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> list(UUID recipientId, NotificationStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<Notification> result = status == null
                ? notificationRepository.findByRecipientId(recipientId, pageable)
                : notificationRepository.findByRecipientIdAndStatus(recipientId, status, pageable);
        return PageResponse.of(result, NotificationSenderServiceImpl::toResponse);
    }

    @Override
    public int markAllAsRead(UUID recipientId) {
        return notificationRepository.markAllAsRead(recipientId, LocalDateTime.now());
    }

    @Override
    public NotificationResponse softDelete(UUID id, UUID currentUserId) {
        if (currentUserId == null) {
            // Defensive: gateway should always forward a user id. If not, treat
            // as forbidden (don't trust an unauthenticated delete).
            throw new org.springframework.security.access.AccessDeniedException(
                    "Current user id is required");
        }
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", id));

        // Already soft-deleted - idempotent return.
        if (n.getStatus() == NotificationStatus.DELETED) {
            return toResponse(n);
        }

        // Authorization check: only the recipient can delete their own notification.
        if (!currentUserId.equals(n.getRecipientId())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Only the recipient can delete this notification");
        }

        n.setStatus(NotificationStatus.DELETED);
        Notification saved = notificationRepository.save(n);
        log.info("Notification {} soft-deleted by user {}", id, currentUserId);
        return toResponse(saved);
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
        smsSender.send(String.valueOf(n.getRecipientId()), n.getTitle(), n.getBody());
    }

    private Set<UUID> resolveComposeRecipients(ComposeNotificationRequest request) {
        Set<UUID> recipients = new LinkedHashSet<>();
        if (request.recipientId() != null) {
            recipients.add(request.recipientId());
        }
        if (request.recipientIds() != null) {
            recipients.addAll(request.recipientIds());
        }
        return recipients;
    }

    private Set<NotificationChannel> resolveComposeChannels(ComposeNotificationRequest request) {
        Set<NotificationChannel> channels = new LinkedHashSet<>();
        if (request.channel() != null) {
            channels.add(request.channel());
        }
        if (request.channels() != null) {
            channels.addAll(request.channels());
        }
        return channels;
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
                n.getCreatedAt());
    }
}
