package com.pcms.reportservice.service.impl;

import com.pcms.reportservice.dto.CreateReportScheduleRequest;
import com.pcms.reportservice.dto.ReportScheduleResponse;
import com.pcms.reportservice.entity.ReportSchedule;
import com.pcms.reportservice.repository.ReportScheduleRepository;
import com.pcms.reportservice.service.ReportDeliveryService;
import com.pcms.reportservice.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportScheduleServiceImplTest {

    @Mock
    private ReportScheduleRepository reportScheduleRepository;

    @Mock
    private ReportService reportService;

    @Mock
    private ReportDeliveryService reportDeliveryService;

    private ReportScheduleServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ReportScheduleServiceImpl(reportScheduleRepository, reportService, reportDeliveryService);
    }

    @Test
    void createMapsCanonicalRequestToCurrentEntityAndResponse() {
        UUID branchId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();
        CreateReportScheduleRequest request = new CreateReportScheduleRequest(
                "revenue", "pdf", branchId, "0 0 9 * * *", "ops@pcms.vn", createdBy);
        when(reportScheduleRepository.save(any(ReportSchedule.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReportScheduleResponse response = service.create(request);

        ArgumentCaptor<ReportSchedule> captor = ArgumentCaptor.forClass(ReportSchedule.class);
        org.mockito.Mockito.verify(reportScheduleRepository).save(captor.capture());
        ReportSchedule saved = captor.getValue();
        assertThat(saved.getType()).isEqualTo("revenue");
        assertThat(saved.getFormat()).isEqualTo("pdf");
        assertThat(saved.getBranchId()).isEqualTo(branchId);
        assertThat(saved.getCronExpression()).isEqualTo("0 0 9 * * *");
        assertThat(saved.getRecipientEmail()).isEqualTo("ops@pcms.vn");
        assertThat(saved.getCreatedBy()).isEqualTo(createdBy);
        assertThat(saved.isActive()).isTrue();
        assertThat(saved.getLastStatus()).isEqualTo("PENDING");
        assertThat(saved.getNextRunAt()).isNotNull();

        assertThat(response.type()).isEqualTo("revenue");
        assertThat(response.format()).isEqualTo("pdf");
        assertThat(response.branchId()).isEqualTo(branchId);
        assertThat(response.cronExpression()).isEqualTo("0 0 9 * * *");
        assertThat(response.recipientEmail()).isEqualTo("ops@pcms.vn");
        assertThat(response.createdBy()).isEqualTo(createdBy);
        assertThat(response.active()).isTrue();
        assertThat(response.lastStatus()).isEqualTo("PENDING");
        assertThat(response.nextRunAt()).isNotNull();
    }

    @Test
    void listMapsCurrentEntitiesToReportScheduleResponse() {
        UUID id = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();
        LocalDateTime lastRunAt = LocalDateTime.of(2026, 7, 19, 9, 0);
        LocalDateTime nextRunAt = LocalDateTime.of(2026, 7, 20, 9, 0);
        LocalDateTime createdAt = LocalDateTime.of(2026, 7, 18, 8, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 7, 19, 9, 1);
        ReportSchedule schedule = new ReportSchedule();
        schedule.setId(id);
        schedule.setType("inventory");
        schedule.setFormat("excel");
        schedule.setBranchId(branchId);
        schedule.setCronExpression("0 0 9 * * *");
        schedule.setRecipientEmail("inventory@pcms.vn");
        schedule.setCreatedBy(createdBy);
        schedule.setActive(true);
        schedule.setLastRunAt(lastRunAt);
        schedule.setNextRunAt(nextRunAt);
        schedule.setLastStatus("SUCCESS");
        schedule.setLastMessage("Delivered");
        schedule.setCreatedAt(createdAt);
        schedule.setUpdatedAt(updatedAt);
        when(reportScheduleRepository.findByActiveTrueOrderByCreatedAtDesc()).thenReturn(List.of(schedule));

        List<ReportScheduleResponse> responses = service.list();

        assertThat(responses).containsExactly(new ReportScheduleResponse(
                id,
                "inventory",
                "excel",
                branchId,
                "0 0 9 * * *",
                "inventory@pcms.vn",
                createdBy,
                true,
                lastRunAt,
                nextRunAt,
                "SUCCESS",
                "Delivered",
                createdAt,
                updatedAt));
    }
}
