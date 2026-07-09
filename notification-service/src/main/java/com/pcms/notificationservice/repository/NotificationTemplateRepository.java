package com.pcms.notificationservice.repository;

import com.pcms.notificationservice.entity.NotificationTemplate;
import com.pcms.notificationservice.enums.NotificationChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, UUID> {

    Optional<NotificationTemplate> findByCodeAndChannelAndActiveTrue(String code, NotificationChannel channel);

    boolean existsByCodeAndChannel(String code, NotificationChannel channel);

    List<NotificationTemplate> findByActiveTrueOrderByCodeAscChannelAsc();

    List<NotificationTemplate> findByChannelAndActiveTrueOrderByCodeAsc(NotificationChannel channel);

    /** B8: Lookup by template key (e.g. NTPL-ORDER-PAID). */
    Optional<NotificationTemplate> findByTemplateKey(String templateKey);
}
