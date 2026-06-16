package com.pcms.orderservice.repository;

import com.pcms.orderservice.entity.OrderSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderSequenceRepository extends JpaRepository<OrderSequence, String> {

    /**
     * B-08: Find + lock the sequence row for the given date prefix.
     * Uses PESSIMISTIC_WRITE which translates to {@code SELECT ... FOR UPDATE} in MySQL.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM OrderSequence s WHERE s.datePrefix = :prefix")
    Optional<OrderSequence> findByIdForUpdate(@Param("prefix") String datePrefix);
}
