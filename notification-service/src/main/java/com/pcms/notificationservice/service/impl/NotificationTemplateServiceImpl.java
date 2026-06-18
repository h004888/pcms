package com.pcms.notificationservice.service.impl;

import com.pcms.common.exception.DuplicateResourceException;
import com.pcms.notificationservice.dto.request.CreateNotificationTemplateRequest;
import com.pcms.notificationservice.dto.response.NotificationTemplateResponse;
import com.pcms.notificationservice.entity.NotificationTemplate;
import com.pcms.notificationservice.enums.NotificationChannel;
import com.pcms.notificationservice.repository.NotificationTemplateRepository;
import com.pcms.notificationservice.service.NotificationTemplateService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NotificationTemplateServiceImpl implements NotificationTemplateService {
    private final NotificationTemplateRepository templateRepository;

    public NotificationTemplateServiceImpl(NotificationTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @PostConstruct
    public void seedDefaultTemplates() {
        createDefaultIfMissing("ORDER_PAID", NotificationChannel.IN_APP,
                "Thanh toán đơn hàng thành công",
                "Đơn hàng {{order_number}} đã thanh toán thành công. Tổng tiền: {{total}} VND.",
                "order_number,total,customer_id,branch_id,paid_at");
        createDefaultIfMissing("ORDER_PAID", NotificationChannel.EMAIL,
                "[PCMS] Thanh toán đơn hàng {{order_number}} thành công",
                "Xin chào, đơn hàng {{order_number}} đã thanh toán thành công với tổng tiền {{total}} VND.",
                "order_number,total,customer_id,branch_id,paid_at");
        createDefaultIfMissing("ORDER_PAID", NotificationChannel.SMS,
                "PCMS: Đơn {{order_number}} đã thanh toán",
                "Don {{order_number}} da thanh toan thanh cong. Tong tien {{total}} VND.",
                "order_number,total");
        createDefaultIfMissing("LOW_STOCK", NotificationChannel.IN_APP,
                "Cảnh báo tồn kho thấp",
                "Thuốc {{medicine_name}} tại chi nhánh {{branch_id}} chỉ còn {{qty_on_hand}} (ngưỡng tối thiểu {{min_qty}}).",
                "medicine_name,branch_id,qty_on_hand,min_qty");
        createDefaultIfMissing("LOW_STOCK", NotificationChannel.EMAIL,
                "[PCMS] Cảnh báo tồn kho thấp - {{medicine_name}}",
                "Thuốc {{medicine_name}} tại chi nhánh {{branch_id}} chỉ còn {{qty_on_hand}}, thấp hơn ngưỡng {{min_qty}}.",
                "medicine_name,branch_id,qty_on_hand,min_qty");
        createDefaultIfMissing("LOW_STOCK", NotificationChannel.SMS,
                "PCMS: Ton kho thap {{medicine_name}}",
                "{{medicine_name}} tai CN {{branch_id}} con {{qty_on_hand}}/{{min_qty}}.",
                "medicine_name,branch_id,qty_on_hand,min_qty");
        createDefaultIfMissing("EXPIRY_ALERT", NotificationChannel.IN_APP,
                "Cảnh báo thuốc sắp hết hạn",
                "Lô {{batch_no}} của thuốc {{medicine_name}} sẽ hết hạn vào {{expiry_date}}.",
                "batch_no,medicine_name,expiry_date,branch_id");
        createDefaultIfMissing("EXPIRY_ALERT", NotificationChannel.EMAIL,
                "[PCMS] Thuốc sắp hết hạn - {{medicine_name}}",
                "Lô {{batch_no}} của thuốc {{medicine_name}} tại chi nhánh {{branch_id}} sẽ hết hạn vào {{expiry_date}}.",
                "batch_no,medicine_name,expiry_date,branch_id");
        createDefaultIfMissing("ORDER_CANCELLED", NotificationChannel.IN_APP,
                "Đơn hàng đã hủy",
                "Đơn hàng {{order_number}} đã được hủy.",
                "order_number");
        createDefaultIfMissing("BROADCAST", NotificationChannel.IN_APP,
                "{{title}}",
                "{{body}}",
                "title,body");
        createDefaultIfMissing("CUSTOMER_TIER_UP", NotificationChannel.IN_APP,
                "Khách hàng lên hạng {{tier}}",
                "Khách hàng {{customer_name}} đã đạt hạng {{tier}} với {{points}} điểm.",
                "customer_name,tier,points");
        createDefaultIfMissing("REPORT_READY", NotificationChannel.IN_APP,
                "Báo cáo đã sẵn sàng",
                "Báo cáo {{report_type}} giai đoạn {{from_date}} - {{to_date}} đã được tạo xong.",
                "report_type,from_date,to_date");
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationTemplateResponse> list(Boolean active, NotificationChannel channel) {
        if (Boolean.FALSE.equals(active)) {
            return templateRepository.findAll().stream()
                    .filter(template -> channel == null || template.getChannel() == channel)
                    .map(NotificationTemplateResponse::from)
                    .toList();
        }
        if (channel != null) {
            return templateRepository.findByChannelAndActiveTrueOrderByCodeAsc(channel).stream()
                    .map(NotificationTemplateResponse::from)
                    .toList();
        }
        return templateRepository.findByActiveTrueOrderByCodeAscChannelAsc().stream()
                .map(NotificationTemplateResponse::from)
                .toList();
    }

    @Override
    public NotificationTemplateResponse create(CreateNotificationTemplateRequest request) {
        if (templateRepository.existsByCodeAndChannel(request.code(), request.channel())) {
            throw new DuplicateResourceException("NotificationTemplate", request.code() + ":" + request.channel());
        }
        NotificationTemplate template = new NotificationTemplate(
                request.code(),
                request.channel(),
                request.titleTemplate(),
                request.bodyTemplate(),
                request.variables());
        template.setActive(request.active() == null || request.active());
        return NotificationTemplateResponse.from(templateRepository.save(template));
    }

    private void createDefaultIfMissing(String code, NotificationChannel channel,
            String titleTemplate, String bodyTemplate, String variables) {
        if (!templateRepository.existsByCodeAndChannel(code, channel)) {
            templateRepository.save(new NotificationTemplate(code, channel, titleTemplate, bodyTemplate, variables));
        }
    }
}