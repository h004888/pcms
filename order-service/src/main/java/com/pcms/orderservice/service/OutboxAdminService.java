package com.pcms.orderservice.service;

import java.util.UUID;

public interface OutboxAdminService {

    UUID retryDeadLetter(UUID deadLetterEventId);
}