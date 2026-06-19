package com.pcms.customerportal.service.impl;

import com.pcms.customerportal.dto.request.UpdateNotificationSettingsPatchRequest;
import com.pcms.customerportal.dto.request.UpdateNotificationSettingsRequest;
import com.pcms.customerportal.dto.response.NotificationSettingsResponse;
import com.pcms.customerportal.entity.CustomerNotificationSetting;
import com.pcms.customerportal.repository.CustomerNotificationSettingRepository;
import com.pcms.customerportal.service.NotificationSettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class NotificationSettingsServiceImpl implements NotificationSettingsService {

    private static final Logger log = LoggerFactory.getLogger(NotificationSettingsServiceImpl.class);

    private final CustomerNotificationSettingRepository repo;

    public NotificationSettingsServiceImpl(CustomerNotificationSettingRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public NotificationSettingsResponse get(UUID currentCustomerId) {
        return NotificationSettingsResponse.from(
                repo.findByCustomerId(currentCustomerId)
                        .orElseGet(() -> createDefault(currentCustomerId)));
    }

    @Override
    @Transactional
    public NotificationSettingsResponse update(UUID currentCustomerId,
                                               UpdateNotificationSettingsRequest req) {
        CustomerNotificationSetting s = repo.findByCustomerId(currentCustomerId)
                .orElseGet(() -> createDefault(currentCustomerId));
        s.setPushEnabled(req.pushEnabled());
        s.setEmailEnabled(req.emailEnabled());
        s.setSmsEnabled(req.smsEnabled());
        s.setMarketingEnabled(req.marketingEnabled());
        s.setOrderUpdates(req.orderUpdates());
        s.setLowStockAlert(req.lowStockAlert());
        s.setExpiryAlert(req.expiryAlert());
        log.info("[notif-settings] fully replaced for customer={}", currentCustomerId);
        return NotificationSettingsResponse.from(repo.save(s));
    }

    @Override
    @Transactional
    public NotificationSettingsResponse patch(UUID currentCustomerId,
                                              UpdateNotificationSettingsPatchRequest req) {
        CustomerNotificationSetting s = repo.findByCustomerId(currentCustomerId)
                .orElseGet(() -> createDefault(currentCustomerId));
        if (req.pushEnabled() != null)        s.setPushEnabled(req.pushEnabled());
        if (req.emailEnabled() != null)       s.setEmailEnabled(req.emailEnabled());
        if (req.smsEnabled() != null)         s.setSmsEnabled(req.smsEnabled());
        if (req.marketingEnabled() != null)   s.setMarketingEnabled(req.marketingEnabled());
        if (req.orderUpdates() != null)       s.setOrderUpdates(req.orderUpdates());
        if (req.lowStockAlert() != null)      s.setLowStockAlert(req.lowStockAlert());
        if (req.expiryAlert() != null)        s.setExpiryAlert(req.expiryAlert());
        log.info("[notif-settings] patched for customer={}", currentCustomerId);
        return NotificationSettingsResponse.from(repo.save(s));
    }

    private CustomerNotificationSetting createDefault(UUID customerId) {
        CustomerNotificationSetting s = new CustomerNotificationSetting();
        s.setCustomerId(customerId);
        // Defaults are already set in entity field initializers
        return repo.save(s);
    }
}
