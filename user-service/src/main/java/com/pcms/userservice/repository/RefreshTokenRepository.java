package com.pcms.userservice.repository;

import com.pcms.userservice.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByJti(String jti);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /** Active = not revoked, not yet expired. Used by password change to force re-login. */
    @Query("SELECT t FROM RefreshToken t WHERE t.userId = :userId AND t.revoked = false AND t.expiresAt > :now")
    List<RefreshToken> findActiveByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE RefreshToken t SET t.revoked = true, t.revokedAt = :revokedAt "
            + "WHERE t.userId = :userId AND t.revoked = false")
    int revokeAllActiveByUserId(@Param("userId") UUID userId, @Param("revokedAt") LocalDateTime revokedAt);
}