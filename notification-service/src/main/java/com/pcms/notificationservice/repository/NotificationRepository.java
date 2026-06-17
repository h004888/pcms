package com.pcms.notificationservice.repository;

import com.pcms.notificationservice.entity.Notification;
import com.pcms.notificationservice.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByRecipientId(UUID recipientId, Pageable pageable);

    Page<Notification> findByRecipientIdAndStatus(UUID recipientId, NotificationStatus status, Pageable pageable);

    List<Notification> findByStatus(NotificationStatus status);

    @Modifying
    @Query("UPDATE Notification n SET n.status = com.pcms.notificationservice.enums.NotificationStatus.READ, n.readAt = :readAt "
            + "WHERE n.recipientId = :recipientId AND n.status <> com.pcms.notificationservice.enums.NotificationStatus.READ")
    int markAllAsRead(UUID recipientId, LocalDateTime readAt);
}
