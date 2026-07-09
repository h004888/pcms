package com.pcms.notificationservice.service;

public interface SmsSender {
    /** Send an SMS. Returns true on success. */
    void send(String recipient, String title, String body);
}
