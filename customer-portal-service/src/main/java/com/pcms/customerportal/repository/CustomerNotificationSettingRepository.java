package com.pcms.customerportal.repository;

import com.pcms.customerportal.entity.CustomerNotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerNotificationSettingRepository
        extends JpaRepository<CustomerNotificationSetting, UUID> {

    Optional<CustomerNotificationSetting> findByCustomerId(UUID customerId);

    boolean existsByCustomerId(UUID customerId);
}
