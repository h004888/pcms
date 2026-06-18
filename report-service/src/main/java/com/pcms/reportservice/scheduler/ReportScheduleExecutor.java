package com.pcms.reportservice.scheduler;

import com.pcms.reportservice.service.ReportScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReportScheduleExecutor {

    private static final Logger log = LoggerFactory.getLogger(ReportScheduleExecutor.class);

    private final ReportScheduleService reportScheduleService;

    public ReportScheduleExecutor(ReportScheduleService reportScheduleService) {
        this.reportScheduleService = reportScheduleService;
    }

    @Scheduled(fixedDelayString = "${pcms.reports.schedule-executor-delay-ms:60000}")
    public void executeDueSchedules() {
        int executed = reportScheduleService.executeDueSchedules();
        if (executed > 0) {
            log.info("Executed {} scheduled report(s)", executed);
        }
    }
}