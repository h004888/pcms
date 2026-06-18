package com.pcms.userservice.repository;

import com.pcms.userservice.entity.User;
import com.pcms.userservice.enums.Role;
import com.pcms.userservice.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByStatus(UserStatus status);

    long countByRole(Role role);

    Page<User> findByStatus(UserStatus status, Pageable pageable);

    List<User> findByStatus(UserStatus status);

    List<User> findByRole(Role role);

    List<User> findByBranchId(UUID branchId);

    Page<User> findByLastLoginAtIsNotNullOrderByLastLoginAtDesc(Pageable pageable);

    @Query("SELECT u FROM User u WHERE " +
            "(:search IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "  OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:role IS NULL OR u.role = :role) " +
            "AND (:branchId IS NULL OR u.branchId = :branchId) " +
            "AND (:status IS NULL OR u.status = :status)")
    Page<User> searchUsers(String search, Role role, UUID branchId, UserStatus status, Pageable pageable);
}
