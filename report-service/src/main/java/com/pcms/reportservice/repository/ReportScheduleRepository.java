package com.pcms.reportservice.repository;

import com.pcms.reportservice.entity.ReportSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ReportScheduleRepository extends JpaRepository<ReportSchedule, UUID> {
    List<ReportSchedule> findByActiveTrue();
    List<ReportSchedule> findByReportTypeAndActiveTrue(String reportType);
}
