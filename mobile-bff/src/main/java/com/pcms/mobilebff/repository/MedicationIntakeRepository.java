package com.pcms.mobilebff.repository;

import com.pcms.mobilebff.entity.MedicationIntake;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MedicationIntakeRepository extends JpaRepository<MedicationIntake, UUID> {
    List<MedicationIntake> findByReminderIdOrderByScheduledAtDesc(UUID reminderId);
}
