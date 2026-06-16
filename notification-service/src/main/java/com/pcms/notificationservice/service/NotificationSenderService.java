package com.pcms.notificationservice.service;

import com.pcms.notificationservice.dto.BroadcastRequest;
import com.pcms.notificationservice.dto.CreateNotificationRequest;
import com.pcms.notificationservice.dto.NotificationResponse;
import com.pcms.common.dto.PageResponse;
import com.pcms.notificationservice.entity.Notification;

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

    /**
     * NSF-09: Retry a previously FAILED notification up to maxAttempts.
     */
    NotificationResponse retry(UUID id);

    /**
     * Mark a notification as READ and persist the timestamp.
     */
    NotificationResponse markAsRead(UUID id);

    /**
     * List notifications for a recipient with paging.
     */
    PageResponse<NotificationResponse> list(UUID recipientId, int page, int size);
}
