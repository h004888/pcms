package com.pcms.notificationservice.service.impl;

import com.pcms.notificationservice.entity.NotificationTemplate;
import com.pcms.notificationservice.enums.NotificationChannel;
import com.pcms.notificationservice.repository.NotificationTemplateRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TemplateResolverImplTest {

    @Test
    void shouldResolveTemplateVariables() {
        NotificationTemplateRepository repository = Mockito.mock(NotificationTemplateRepository.class);
        Mockito.when(repository.findByCodeAndChannelAndActiveTrue("ORDER_PAID", NotificationChannel.IN_APP))
                .thenReturn(Optional.of(new NotificationTemplate(
                        "ORDER_PAID",
                        NotificationChannel.IN_APP,
                        "Đơn {{order_number}} thành công",
                        "Tổng tiền {{total}}",
                        "order_number,total")));

        TemplateResolverImpl resolver = new TemplateResolverImpl(repository);
        var resolved = resolver.resolve("ORDER_PAID", NotificationChannel.IN_APP, null, null,
                Map.of("order_number", "ORD-001", "total", 50000));

        assertEquals("Đơn ORD-001 thành công", resolved.title());
        assertEquals("Tổng tiền 50000", resolved.body());
    }

    @Test
    void shouldFallbackWhenTemplateMissing() {
        NotificationTemplateRepository repository = Mockito.mock(NotificationTemplateRepository.class);
        Mockito.when(repository.findByCodeAndChannelAndActiveTrue("MISSING", NotificationChannel.IN_APP))
                .thenReturn(Optional.empty());

        TemplateResolverImpl resolver = new TemplateResolverImpl(repository);
        var resolved = resolver.resolve("MISSING", NotificationChannel.IN_APP,
                "Fallback {{name}}", "Body {{value}}", Map.of("name", "Test", "value", 123));

        assertEquals("Fallback Test", resolved.title());
        assertEquals("Body 123", resolved.body());
    }
}