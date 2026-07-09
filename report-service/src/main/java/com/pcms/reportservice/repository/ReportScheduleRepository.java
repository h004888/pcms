package com.pcms.reportservice.repository;

import com.pcms.reportservice.entity.ReportSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReportScheduleRepository extends JpaRepository<ReportSchedule, UUID> {

    List<ReportSchedule> findByActiveTrueOrderByCreatedAtDesc();

    List<ReportSchedule> findByActiveTrueAndNextRunAtLessThanEqualOrderByNextRunAtAsc(LocalDateTime now);

    /** B9: Find by report type. */
    List<ReportSchedule> findByTypeAndActiveTrue(String type);
}
