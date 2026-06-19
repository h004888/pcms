package com.pcms.pharmacistworkbench.service;

import com.pcms.common.exception.ResourceNotFoundException;
import com.pcms.pharmacistworkbench.dto.request.ScheduleFollowUpRequest;
import com.pcms.pharmacistworkbench.dto.response.FollowUpTaskResponse;
import com.pcms.pharmacistworkbench.entity.FollowUpTask;
import com.pcms.pharmacistworkbench.repository.FollowUpTaskRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FollowUpService {

    private final FollowUpTaskRepository repository;

    public FollowUpService(FollowUpTaskRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public FollowUpTaskResponse schedule(ScheduleFollowUpRequest request) {
        FollowUpTask task = new FollowUpTask();
        task.setCustomerId(request.customerId());
        task.setOrderId(request.orderId());
        task.setPrescriptionId(request.prescriptionId());
        task.setType(request.type() != null ? request.type() : "FEEDBACK");
        task.setScheduledAt(LocalDateTime.now().plusDays(request.daysFromNow()));
        task.setStatus("PENDING");
        task = repository.save(task);
        return toResponse(task);
    }

    @Transactional(readOnly = true)
    public List<FollowUpTaskResponse> listByCustomer(UUID customerId) {
        return repository.findByCustomerIdOrderByScheduledAtDesc(customerId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    public FollowUpTaskResponse recordResponse(UUID taskId, String response, String note) {
        FollowUpTask task = repository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("FollowUpTask", taskId));
        task.setResponse(response);
        task.setNote(note);
        task.setStatus("COMPLETED");
        return toResponse(repository.save(task));
    }

    @Transactional
    public void cancel(UUID taskId) {
        FollowUpTask task = repository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("FollowUpTask", taskId));
        task.setStatus("CANCELLED");
        repository.save(task);
    }

    /**
     * Cron job: every 5 minutes, mark PENDING tasks past their scheduled time as SENT.
     * In production, this would publish to notification-service.
     */
    @Scheduled(fixedDelay = 5 * 60 * 1000)
    @Transactional
    public void dispatchPending() {
        List<FollowUpTask> pending = repository.findByStatusAndScheduledAtBefore("PENDING", LocalDateTime.now());
        pending.forEach(t -> {
            t.setStatus("SENT");
            repository.save(t);
        });
    }

    private FollowUpTaskResponse toResponse(FollowUpTask t) {
        return new FollowUpTaskResponse(t.getId(), t.getCustomerId(), t.getOrderId(),
                t.getPrescriptionId(), t.getType(), t.getScheduledAt(),
                t.getStatus(), t.getResponse(), t.getNote());
    }
}
