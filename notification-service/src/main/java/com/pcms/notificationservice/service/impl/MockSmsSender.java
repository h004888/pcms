package com.pcms.notificationservice.service.impl;

import com.pcms.notificationservice.service.SmsSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MockSmsSender implements SmsSender {

    private static final Logger log = LoggerFactory.getLogger(MockSmsSender.class);

    @Override
    public void send(String recipient, String title, String body) {
        log.info("SMS mock sent to {} | title={} | body={}", recipient, title, body);
    }
}