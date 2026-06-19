package com.pcms.mobilebff.service;

import com.pcms.common.exception.ResourceNotFoundException;
import com.pcms.mobilebff.dto.request.CreateReminderRequest;
import com.pcms.mobilebff.dto.response.ReminderResponse;
import com.pcms.mobilebff.entity.MedicationReminder;
import com.pcms.mobilebff.repository.MedicationReminderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ReminderService {

    private final MedicationReminderRepository repository;

    public ReminderService(MedicationReminderRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ReminderResponse create(CreateReminderRequest request) {
        MedicationReminder r = new MedicationReminder();
        r.setCustomerId(request.customerId());
        r.setFamilyMemberId(request.familyMemberId());
        r.setMedicineName(request.medicineName());
        r.setDosage(request.dosage());
        r.setFrequency(request.frequency());
        r.setScheduleTime(request.scheduleTime());
        r.setStartDate(request.startDate());
        r.setEndDate(request.endDate());
        r.setNotes(request.notes());
        r.setActive(true);
        return toResponse(repository.save(r));
    }

    @Transactional(readOnly = true)
    public List<ReminderResponse> listByCustomer(UUID customerId) {
        return repository.findByCustomerIdAndActiveOrderByStartDateDesc(customerId, true)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public ReminderResponse deactivate(UUID id) {
        MedicationReminder r = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder", id));
        r.setActive(false);
        return toResponse(repository.save(r));
    }

    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Reminder", id);
        }
        repository.deleteById(id);
    }

    private ReminderResponse toResponse(MedicationReminder r) {
        return new ReminderResponse(
                r.getId(), r.getCustomerId(), r.getFamilyMemberId(),
                r.getMedicineName(), r.getDosage(), r.getFrequency(),
                r.getScheduleTime(), r.getStartDate(), r.getEndDate(),
                r.getActive(), r.getNotes(), r.getCreatedAt()
        );
    }
}
