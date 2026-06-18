package com.pcms.reportservice.service.impl;

import com.pcms.common.exception.InvalidOperationException;
import com.pcms.reportservice.entity.ReportSchedule;
import com.pcms.reportservice.service.ReportDeliveryService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ReportDeliveryServiceImpl implements ReportDeliveryService {

    private final JavaMailSender mailSender;

    public ReportDeliveryServiceImpl(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSender = mailSenderProvider.getIfAvailable();
    }

    @Override
    public String deliverSchedule(ReportSchedule schedule, byte[] content, LocalDate fromDate, LocalDate toDate) {
        if (mailSender == null) {
            throw new InvalidOperationException(
                    "Report generated but mail sender is not configured",
                    "Đã tạo báo cáo nhưng chưa cấu hình máy chủ email");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(schedule.getRecipientEmail());
            helper.setSubject(buildSubject(schedule, fromDate, toDate));
            helper.setText(buildBody(schedule, fromDate, toDate), false);
            helper.addAttachment(buildFilename(schedule), new ByteArrayResource(content));
            mailSender.send(message);
            return "Delivered " + content.length + " bytes to " + schedule.getRecipientEmail();
        } catch (MessagingException | MailException ex) {
            throw new InvalidOperationException(
                    "Failed to deliver scheduled report: " + ex.getMessage(),
                    "Gửi báo cáo theo lịch thất bại: " + ex.getMessage());
        }
    }

    private String buildSubject(ReportSchedule schedule, LocalDate fromDate, LocalDate toDate) {
        return "PCMS " + schedule.getType() + " report (" + fromDate + " - " + toDate + ")";
    }

    private String buildBody(ReportSchedule schedule, LocalDate fromDate, LocalDate toDate) {
        return "Báo cáo " + schedule.getType() + " từ " + fromDate + " đến " + toDate
                + " được tạo tự động bởi PCMS.";
    }

    private String buildFilename(ReportSchedule schedule) {
        String extension = "pdf".equalsIgnoreCase(schedule.getFormat()) ? "pdf" : "xlsx";
        return schedule.getType() + "-report." + extension;
    }
}