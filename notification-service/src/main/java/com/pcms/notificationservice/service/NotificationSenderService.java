package com.pcms.notificationservice.service;

import com.pcms.common.dto.PageResponse;
import com.pcms.notificationservice.dto.request.BroadcastRequest;
import com.pcms.notificationservice.dto.request.ComposeNotificationRequest;
import com.pcms.notificationservice.dto.request.CreateNotificationRequest;
import com.pcms.notificationservice.dto.response.NotificationResponse;
import com.pcms.notificationservice.entity.Notification;
import com.pcms.notificationservice.enums.NotificationStatus;

import java.util.UUID;

/**
 * Notification sender service (UC13 - NSF-09).
 * <p>
 * Original implementation was a single class. Per refactor, this is now an
 * interface; the implementation lives in
 * {@link com.pcms.notificationservice.service.impl.NotificationSenderServiceImpl}.
 */
public interface NotificationSenderService {

    /**
     * Send a single notification (async, with NSF-09 retry/backoff).
     */
    void send(Notification notification);

    /**
     * Persist + send a notification from a request payload. Returns the
     * resulting response.
     */
    NotificationResponse createAndSend(CreateNotificationRequest request);

    /**
     * Broadcast to an audience (resolved to a set of recipients) using the
     * provided channels. Returns the count of queued notifications.
     */
    int broadcast(BroadcastRequest request);

    int compose(ComposeNotificationRequest request);

    /**
     * NSF-09: Retry a previously FAILED notification up to maxAttempts.
     */
    NotificationResponse retry(UUID id);

    NotificationResponse getById(UUID id);

    /**
     * Mark a notification as READ and persist the timestamp.
     */
    NotificationResponse markAsRead(UUID id);

    /**
     * List notifications for a recipient with paging.
     */
    PageResponse<NotificationResponse> list(UUID recipientId, int page, int size);

    PageResponse<NotificationResponse> list(UUID recipientId, NotificationStatus status, int page, int size);

    int markAllAsRead(UUID recipientId);

    /**
     * TICKET-304: Soft-delete a notification for the given current user.
     * Only the recipient can delete their own notification; other callers
     * receive an {@code AccessDeniedException} (mapped to 403 by the
     * GlobalExceptionHandler).
     *
     * @param id            notification id
     * @param currentUserId id of the user performing the delete (from JWT)
     * @return updated {@link NotificationResponse}
     * @throws ResourceNotFoundException if the notification does not exist
     * @throws org.springframework.security.access.AccessDeniedException if the
     *         current user is not the recipient
     */
    NotificationResponse softDelete(UUID id, UUID currentUserId);
}
