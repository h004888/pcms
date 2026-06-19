package com.pcms.pharmacistworkbench.repository;

import com.pcms.pharmacistworkbench.entity.FollowUpTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface FollowUpTaskRepository extends JpaRepository<FollowUpTask, UUID> {
    List<FollowUpTask> findByCustomerIdOrderByScheduledAtDesc(UUID customerId);
    List<FollowUpTask> findByStatusAndScheduledAtBefore(String status, LocalDateTime before);
    List<FollowUpTask> findByPharmacistIdOrderByScheduledAtDesc(UUID pharmacistId);
}
