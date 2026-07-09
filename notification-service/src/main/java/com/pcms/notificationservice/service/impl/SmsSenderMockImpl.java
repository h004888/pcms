package com.pcms.notificationservice.service.impl;

import com.pcms.notificationservice.service.SmsSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "notification.channels.sms", havingValue = "true", matchIfMissing = false)
public class SmsSenderMockImpl implements SmsSender {
    private static final Logger log = LoggerFactory.getLogger(SmsSenderMockImpl.class);

    @Override
    public void send(String toPhoneNumber, String message) {
        log.info("[SMS-MOCK] To: {} | Message: {}", toPhoneNumber, message);
        // In production: integrate with VNPT SMS, Viettel, or Twilio
    }
}
