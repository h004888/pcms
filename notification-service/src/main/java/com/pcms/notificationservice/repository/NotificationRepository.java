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

    /**
     * TICKET-304: exclude DELETED (soft-deleted) notifications from listings.
     */
    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId AND n.status <> com.pcms.notificationservice.enums.NotificationStatus.DELETED")
    Page<Notification> findByRecipientId(UUID recipientId, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId AND n.status = :status AND n.status <> com.pcms.notificationservice.enums.NotificationStatus.DELETED")
    Page<Notification> findByRecipientIdAndStatus(UUID recipientId, NotificationStatus status, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.status <> com.pcms.notificationservice.enums.NotificationStatus.DELETED")
    List<Notification> findByStatus(NotificationStatus status);

    @Modifying
    @Query("UPDATE Notification n SET n.status = com.pcms.notificationservice.enums.NotificationStatus.READ, n.readAt = :readAt "
            + "WHERE n.recipientId = :recipientId AND n.status <> com.pcms.notificationservice.enums.NotificationStatus.READ")
    int markAllAsRead(UUID recipientId, LocalDateTime readAt);
}
