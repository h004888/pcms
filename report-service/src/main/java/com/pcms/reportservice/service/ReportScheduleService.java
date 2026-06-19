package com.pcms.reportservice.service;

import com.pcms.reportservice.dto.CreateReportScheduleRequest;
import com.pcms.reportservice.dto.ReportScheduleResponse;

import java.util.List;
import java.util.UUID;

public interface ReportScheduleService {
    ReportScheduleResponse create(CreateReportScheduleRequest request);

    List<ReportScheduleResponse> list();

    int executeDueSchedules();

    /**
     * TICKET-306: Cancel (soft-delete) a schedule by flipping its
     * {@code active} flag to {@code false}. The schedule stays in the
     * database for audit purposes but is no longer picked up by
     * {@link #executeDueSchedules()}.
     *
     * @param id schedule id
     * @return the updated {@link ReportScheduleResponse} with {@code active=false}
     * @throws com.pcms.common.exception.ResourceNotFoundException if not found
     */
    ReportScheduleResponse cancel(UUID id);
}