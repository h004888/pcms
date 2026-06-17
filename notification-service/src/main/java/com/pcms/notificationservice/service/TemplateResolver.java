package com.pcms.notificationservice.service;

import com.pcms.notificationservice.entity.NotificationTemplate;
import com.pcms.notificationservice.repository.NotificationTemplateRepository;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * B8: Resolves a template key to title/body with variable substitution.
 * Variables are {{key}} patterns replaced from the given map.
 */
@Component
public class TemplateResolver {

    private final NotificationTemplateRepository templateRepository;

    public TemplateResolver(NotificationTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    public record Resolved(String title, String body) {}

    public Optional<Resolved> resolve(String templateKey, Map<String, String> vars) {
        return templateRepository.findByTemplateKey(templateKey)
                .map(t -> new Resolved(
                        substitute(t.getTitleTemplate(), vars),
                        substitute(t.getBodyTemplate(), vars)
                ));
    }

    private String substitute(String template, Map<String, String> vars) {
        if (template == null || vars == null) return template;
        String result = template;
        for (Map.Entry<String, String> e : vars.entrySet()) {
            result = result.replace("{{" + e.getKey() + "}}", e.getValue() != null ? e.getValue() : "");
        }
        return result;
    }
}
