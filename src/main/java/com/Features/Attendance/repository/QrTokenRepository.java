package com.Features.Attendance.repository;

import com.Features.Attendance.model.QrToken;
import com.Features.Attendance.model.QrTokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QrTokenRepository extends JpaRepository<QrToken, UUID> {

    /** Find the ACTIVE token for a slot on today's date */
    Optional<QrToken> findBySlotIdAndSessionDateAndStatus(
            UUID slotId, LocalDate sessionDate, QrTokenStatus status);

    /** Find a token by its encoded value (used during student scan) */
    Optional<QrToken> findByTokenValue(String tokenValue);

    /** All ACTIVE tokens across all slots — admin management view */
    List<QrToken> findByStatusOrderByCreatedAtDesc(QrTokenStatus status);

    /** All tokens for a session (same sessionId, all rotations) */
    List<QrToken> findBySessionIdOrderByCreatedAtDesc(UUID sessionId);

    /**
     * All ACTIVE tokens that are within threshold of expiry.
     * Used by the scheduled rotation task.
     */
    @Query("""
           SELECT t FROM QrToken t
           WHERE t.status = 'ACTIVE'
             AND t.expiresAt <= :threshold
           """)
    List<QrToken> findTokensNearingExpiry(@Param("threshold") LocalDateTime threshold);

    /** Check if an ACTIVE session exists for this slot today */
    boolean existsBySlotIdAndSessionDateAndStatus(
            UUID slotId, LocalDate sessionDate, QrTokenStatus status);
}
