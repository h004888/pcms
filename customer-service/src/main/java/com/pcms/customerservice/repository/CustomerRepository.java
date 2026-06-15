package com.pcms.customerservice.repository;

import com.pcms.customerservice.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByPhone(String phone);
    Optional<Customer> findByCode(String code);
    boolean existsByPhone(String phone);

    /** UC08: Search customers by name/phone/code */
    @Query("SELECT c FROM Customer c WHERE " +
           "(:search IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "  OR c.phone LIKE CONCAT('%', :search, '%') " +
           "  OR LOWER(c.code) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Customer> search(String search, Pageable pageable);

    /** Used for code generation CUST-yyyy#### */
    @Query("SELECT c FROM Customer c WHERE c.code LIKE CONCAT('CUST-', :yearPrefix, '%') ORDER BY c.code DESC")
    List<Customer> findByYearPrefix(String yearPrefix, Pageable pageable);
}
