package com.pcms.notificationservice.repository;

import com.pcms.notificationservice.entity.OutboxLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OutboxLogRepository extends JpaRepository<OutboxLog, UUID> {

    boolean existsByEventId(UUID eventId);
}