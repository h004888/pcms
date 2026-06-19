package com.pcms.reportservice.service.impl;

import com.pcms.common.exception.InvalidOperationException;
import com.pcms.common.exception.ResourceNotFoundException;
import com.pcms.reportservice.dto.CreateReportScheduleRequest;
import com.pcms.reportservice.dto.ReportScheduleResponse;
import com.pcms.reportservice.entity.ReportSchedule;
import com.pcms.reportservice.repository.ReportScheduleRepository;
import com.pcms.reportservice.service.ReportDeliveryService;
import com.pcms.reportservice.service.ReportScheduleService;
import com.pcms.reportservice.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ReportScheduleServiceImpl implements ReportScheduleService {

    private static final Logger log = LoggerFactory.getLogger(ReportScheduleServiceImpl.class);
    private static final int DEFAULT_RANGE_DAYS = 7;

    private final ReportScheduleRepository reportScheduleRepository;
    private final ReportService reportService;
    private final ReportDeliveryService reportDeliveryService;

    public ReportScheduleServiceImpl(ReportScheduleRepository reportScheduleRepository,
            ReportService reportService,
            ReportDeliveryService reportDeliveryService) {
        this.reportScheduleRepository = reportScheduleRepository;
        this.reportService = reportService;
        this.reportDeliveryService = reportDeliveryService;
    }

    @Override
    public ReportScheduleResponse create(CreateReportScheduleRequest request) {
        if (!"revenue".equalsIgnoreCase(request.type())
                && !"inventory".equalsIgnoreCase(request.type())
                && !"staff".equalsIgnoreCase(request.type())) {
            throw new InvalidOperationException(
                    "Unsupported report schedule type",
                    "Loại lịch báo cáo không được hỗ trợ");
        }
        ReportSchedule schedule = new ReportSchedule();
        schedule.setType(request.type().trim().toLowerCase());
        schedule.setFormat(request.format().trim().toLowerCase());
        schedule.setBranchId(request.branchId());
        schedule.setCronExpression(request.cronExpression().trim());
        schedule.setRecipientEmail(request.recipientEmail().trim());
        schedule.setCreatedBy(request.createdBy());
        schedule.setActive(true);
        schedule.setNextRunAt(resolveNextRunAt(schedule.getCronExpression(), LocalDateTime.now()));
        schedule.setLastStatus("PENDING");
        return ReportScheduleResponse.from(reportScheduleRepository.save(schedule));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportScheduleResponse> list() {
        return reportScheduleRepository.findByActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(ReportScheduleResponse::from)
                .toList();
    }

    @Override
    public ReportScheduleResponse cancel(UUID id) {
        ReportSchedule schedule = reportScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReportSchedule", id));
        if (!schedule.isActive()) {
            // Idempotent: already cancelled.
            return ReportScheduleResponse.from(schedule);
        }
        schedule.setActive(false);
        schedule.setLastStatus("CANCELLED");
        schedule.setLastMessage("Cancelled by user");
        log.info("Report schedule {} cancelled", id);
        return ReportScheduleResponse.from(reportScheduleRepository.save(schedule));
    }

    @Override
    public int executeDueSchedules() {
        LocalDateTime now = LocalDateTime.now();
        List<ReportSchedule> dueSchedules = reportScheduleRepository
                .findByActiveTrueAndNextRunAtLessThanEqualOrderByNextRunAtAsc(now);

        int executed = 0;
        for (ReportSchedule schedule : dueSchedules) {
            execute(schedule, now);
            executed++;
        }
        return executed;
    }

    private void execute(ReportSchedule schedule, LocalDateTime now) {
        try {
            LocalDate to = now.toLocalDate();
            LocalDate from = to.minusDays(DEFAULT_RANGE_DAYS);
            byte[] exported = (byte[]) reportService.export(schedule.getType(), schedule.getFormat(), from, to);
            String deliveryMessage = reportDeliveryService.deliverSchedule(schedule, exported, from, to);
            schedule.setLastRunAt(now);
            schedule.setLastStatus("SUCCESS");
            schedule.setLastMessage(deliveryMessage);
            schedule.setNextRunAt(resolveNextRunAt(schedule.getCronExpression(), now));
        } catch (RuntimeException ex) {
            log.warn("Scheduled report {} failed: {}", schedule.getId(), ex.getMessage());
            schedule.setLastRunAt(now);
            schedule.setLastStatus("FAILED");
            schedule.setLastMessage(ex.getMessage());
            schedule.setNextRunAt(resolveNextRunAt(schedule.getCronExpression(), now));
        }
        reportScheduleRepository.save(schedule);
    }

    private LocalDateTime resolveNextRunAt(String cronExpression, LocalDateTime baseTime) {
        try {
            LocalDateTime nextRunAt = CronExpression.parse(cronExpression).next(baseTime);
            if (nextRunAt == null) {
                throw new IllegalArgumentException("Cron expression has no next execution time");
            }
            return nextRunAt;
        } catch (IllegalArgumentException ex) {
            throw new InvalidOperationException(
                    "Invalid cron expression: " + cronExpression,
                    "Biểu thức cron không hợp lệ: " + cronExpression);
        }
    }
}