package com.pcms.reportservice.service;

import com.pcms.reportservice.entity.ReportSchedule;

import java.time.LocalDate;

public interface ReportDeliveryService {

    String deliverSchedule(ReportSchedule schedule, byte[] content, LocalDate fromDate, LocalDate toDate);
}