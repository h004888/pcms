package com.pcms.reportservice.service;

import com.pcms.reportservice.dto.CreateReportScheduleRequest;
import com.pcms.reportservice.dto.ReportScheduleResponse;

import java.util.List;

public interface ReportScheduleService {
    ReportScheduleResponse create(CreateReportScheduleRequest request);

    List<ReportScheduleResponse> list();

    int executeDueSchedules();
}