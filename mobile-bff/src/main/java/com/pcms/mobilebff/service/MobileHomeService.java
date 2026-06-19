package com.pcms.mobilebff.service;

import com.pcms.mobilebff.client.BranchClient;
import com.pcms.mobilebff.client.NotificationClient;
import com.pcms.mobilebff.client.OrderClient;
import com.pcms.mobilebff.dto.response.MobileHomeResponse;
import com.pcms.mobilebff.dto.response.ReminderResponse;
import com.pcms.mobilebff.entity.MedicationReminder;
import com.pcms.mobilebff.repository.MedicationReminderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MobileHomeService {

    private static final Logger log = LoggerFactory.getLogger(MobileHomeService.class);

    private final NotificationClient notificationClient;
    private final OrderClient orderClient;
    private final BranchClient branchClient;
    private final MedicationReminderRepository reminderRepository;

    public MobileHomeService(NotificationClient notificationClient, OrderClient orderClient,
                             BranchClient branchClient, MedicationReminderRepository reminderRepository) {
        this.notificationClient = notificationClient;
        this.orderClient = orderClient;
        this.branchClient = branchClient;
        this.reminderRepository = reminderRepository;
    }

    @Transactional(readOnly = true)
    public MobileHomeResponse getHome(UUID customerId) {
        // Aggregate from multiple services
        Map<String, Object> notifPage = safeGet(() -> notificationClient.list(customerId.toString(), 0, 5));
        Map<String, Object> orderPage = safeGet(() -> orderClient.list(customerId.toString(), 0, 5));
        Map<String, Object> branchPage = safeGet(() -> branchClient.list(0, 5));

        List<MedicationReminder> reminders = reminderRepository
                .findByCustomerIdAndActiveOrderByStartDateDesc(customerId, true);

        @SuppressWarnings("unchecked")
        List<Object> notifications = (List<Object>) notifPage.getOrDefault("data", List.of());
        @SuppressWarnings("unchecked")
        List<Object> orders = (List<Object>) orderPage.getOrDefault("data", List.of());
        @SuppressWarnings("unchecked")
        List<Object> branches = (List<Object>) branchPage.getOrDefault("data", List.of());

        return new MobileHomeResponse(
                customerId.toString(),
                "CUSTOMER",
                notifications,
                orders,
                reminders.stream().map(this::toReminderResponse).toList(),
                branches,
                null
        );
    }

    private ReminderResponse toReminderResponse(MedicationReminder r) {
        return new ReminderResponse(
                r.getId(), r.getCustomerId(), r.getFamilyMemberId(),
                r.getMedicineName(), r.getDosage(), r.getFrequency(),
                r.getScheduleTime(), r.getStartDate(), r.getEndDate(),
                r.getActive(), r.getNotes(), r.getCreatedAt()
        );
    }

    private Map<String, Object> safeGet(java.util.function.Supplier<Map<String, Object>> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            log.warn("Failed to fetch aggregated data: {}", e.getMessage());
            return Map.of();
        }
    }
}
