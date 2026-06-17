package com.pcms.notificationservice.service.impl;

import com.pcms.notificationservice.dto.response.ResolvedTemplateResponse;
import com.pcms.notificationservice.entity.NotificationTemplate;
import com.pcms.notificationservice.enums.NotificationChannel;
import com.pcms.notificationservice.repository.NotificationTemplateRepository;
import com.pcms.notificationservice.service.TemplateResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class TemplateResolverImpl implements TemplateResolver {
    private static final Logger log = LoggerFactory.getLogger(TemplateResolverImpl.class);

    private final NotificationTemplateRepository templateRepository;

    public TemplateResolverImpl(NotificationTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ResolvedTemplateResponse resolve(String templateCode, NotificationChannel channel,
            String fallbackTitle, String fallbackBody, Map<String, Object> variables) {
        String title = fallbackTitle;
        String body = fallbackBody;
        if (templateCode != null && !templateCode.isBlank()) {
            var template = templateRepository.findByCodeAndChannelAndActiveTrue(templateCode, channel);
            if (template.isPresent()) {
                NotificationTemplate found = template.get();
                title = found.getTitleTemplate();
                body = found.getBodyTemplate();
            } else {
                log.warn("Notification template {} for channel {} not found; using fallback content",
                        templateCode, channel);
            }
        }
        return new ResolvedTemplateResponse(templateCode, replaceVariables(title, variables),
                replaceVariables(body, variables));
    }

    private String replaceVariables(String content, Map<String, Object> variables) {
        if (content == null) {
            return "";
        }
        if (variables == null || variables.isEmpty()) {
            return content;
        }
        String resolved = content;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            resolved = resolved.replace(placeholder, String.valueOf(entry.getValue()));
        }
        return resolved;
    }
}