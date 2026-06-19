package com.pcms.mobilebff.repository;

import com.pcms.mobilebff.entity.MedicationReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MedicationReminderRepository extends JpaRepository<MedicationReminder, UUID> {
    List<MedicationReminder> findByCustomerIdAndActiveOrderByStartDateDesc(UUID customerId, Boolean active);
    List<MedicationReminder> findByFamilyMemberIdAndActiveOrderByStartDateDesc(UUID familyMemberId, Boolean active);
}
