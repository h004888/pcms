package com.pcms.userservice.repository;

import com.pcms.userservice.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link EmailVerificationToken} (TICKET-103).
 *
 * <p>Lookups are performed by token hash - the plain token is never stored
 * (see CR-08). The repository also exposes a bulk-delete used by
 * {@code resendVerification} to invalidate any outstanding tokens for a
 * user before issuing a new one (one-active-token invariant).
 */
@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {

    Optional<EmailVerificationToken> findFirstByTokenHashAndUsedAtIsNull(String tokenHash);

    @Modifying
    @Query("DELETE FROM EmailVerificationToken t WHERE t.userId = :userId AND t.usedAt IS NULL")
    int deleteAllActiveByUserId(@Param("userId") UUID userId);
}
