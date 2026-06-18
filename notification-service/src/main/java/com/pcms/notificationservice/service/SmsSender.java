package com.pcms.notificationservice.service;

public interface SmsSender {
    void send(String recipient, String title, String body);
}