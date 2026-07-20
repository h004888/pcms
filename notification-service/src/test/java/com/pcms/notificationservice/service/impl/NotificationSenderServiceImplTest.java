package com.pcms.notificationservice.service.impl;

import com.pcms.common.exception.InvalidOperationException;
import com.pcms.notificationservice.dto.request.CreateNotificationRequest;
import com.pcms.notificationservice.dto.response.NotificationResponse;
import com.pcms.notificationservice.entity.Notification;
import com.pcms.notificationservice.enums.NotificationChannel;
import com.pcms.notificationservice.enums.NotificationStatus;
import com.pcms.notificationservice.repository.NotificationRepository;
import com.pcms.notificationservice.service.SmsSender;
import com.pcms.notificationservice.service.TemplateResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for NotificationSenderServiceImpl (NSF-09).
 * JavaMailSender is injected as null — EMAIL channel skips mail send gracefully.
 * @Async is effectively synchronous in a plain Mockito test (no Spring context).
 */
@ExtendWith(MockitoExtension.class)
class NotificationSenderServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private TemplateResolver templateResolver;
    @Mock
    private SmsSender smsSender;
    @Mock
    private ObjectProvider<JavaMailSender> mailSenderProvider;

    // mailSender = null — mail not configured; service logs a warning and continues
    private NotificationSenderServiceImpl senderService;

    @BeforeEach
    void setUp() {
        // maxAttempts=3, backoffMs=0 to avoid Thread.sleep in tests
        senderService = new NotificationSenderServiceImpl(
                notificationRepository, templateResolver, smsSender, mailSenderProvider, 3, 0L);
    }

    // -------------------------------------------------------------------------
    // createAndSend() tests
    // -------------------------------------------------------------------------

    @Test
    void createAndSend_persistsAndSendsInApp() {
        // Arrange
        UUID recipientId = UUID.randomUUID();
        CreateNotificationRequest request = new CreateNotificationRequest(
                recipientId,
                NotificationChannel.IN_APP,
                "NTPL-TEST",
                "Test title",
                "Test body",
                null);

        Notification savedNotification = new Notification();
        savedNotification.setId(UUID.randomUUID());
        savedNotification.setRecipientId(recipientId);
        savedNotification.setChannel(NotificationChannel.IN_APP);
        savedNotification.setTemplate("NTPL-TEST");
        savedNotification.setTitle("Test title");
        savedNotification.setBody("Test body");
        savedNotification.setStatus(NotificationStatus.PENDING);

        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(inv -> {
                    Notification n = inv.getArgument(0);
                    if (n.getId() == null) n.setId(UUID.randomUUID());
                    return n;
                });

        // Act
        NotificationResponse response = senderService.createAndSend(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.recipientId()).isEqualTo(recipientId);
        assertThat(response.channel()).isEqualTo(NotificationChannel.IN_APP);
        assertThat(response.title()).isEqualTo("Test title");

        // save() called at least twice: once to persist PENDING, once after send to mark SENT
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void createAndSend_withNullRecipient_throws() {
        // Arrange
        CreateNotificationRequest request = new CreateNotificationRequest(
                null,   // null recipientId
                NotificationChannel.IN_APP,
                null,
                "Title",
                "Body",
                null);

        // Act + Assert
        assertThatThrownBy(() -> senderService.createAndSend(request))
                .isInstanceOf(InvalidOperationException.class);

        verify(notificationRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // markAsRead() tests
    // -------------------------------------------------------------------------

    @Test
    void markAsRead_updatesStatus() {
        // Arrange
        UUID notifId = UUID.randomUUID();
        Notification notification = new Notification();
        notification.setId(notifId);
        notification.setRecipientId(UUID.randomUUID());
        notification.setChannel(NotificationChannel.IN_APP);
        notification.setTitle("Low stock alert");
        notification.setBody("Item X is low");
        notification.setStatus(NotificationStatus.SENT);

        when(notificationRepository.findById(notifId)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        NotificationResponse response = senderService.markAsRead(notifId);

        // Assert
        assertThat(response.status()).isEqualTo(NotificationStatus.READ);
        assertThat(response.readAt()).isNotNull();

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(NotificationStatus.READ);
    }

    @Test
    void markAsRead_alreadyRead_returnsWithoutChange() {
        // Arrange
        UUID notifId = UUID.randomUUID();
        Notification notification = new Notification();
        notification.setId(notifId);
        notification.setRecipientId(UUID.randomUUID());
        notification.setChannel(NotificationChannel.IN_APP);
        notification.setTitle("Already read");;
        notification.setBody("Body text");
        notification.setStatus(NotificationStatus.READ);

        when(notificationRepository.findById(notifId)).thenReturn(Optional.of(notification));

        // Act
        NotificationResponse response = senderService.markAsRead(notifId);

        // Assert — already READ, service returns without calling save
        assertThat(response.status()).isEqualTo(NotificationStatus.READ);
        verify(notificationRepository, never()).save(any());
    }
}
