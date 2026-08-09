package com.Features.Attendance.model;

import com.Features.Teacher.model.Teacher;
import com.Features.Timetable.model.TimetableSlot;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A short-lived, rotating QR token for one live class session.
 *
 * Lifecycle:
 *   Teacher starts attendance → QrToken created (status = ACTIVE)
 *   Auto-rotates every N minutes → old token EXPIRED, new token ACTIVE
 *   Teacher ends attendance → current token REVOKED
 *
 * The QR encodes the URL:
 *   https://yourdomain.com/attend?token=<tokenValue>
 *
 * Dynamic rotation:
 *   A scheduled task (@Scheduled) checks every 30s for tokens
 *   within ROTATION_THRESHOLD of expiry and auto-rotates them.
 *   Teacher's browser polls GET /api/teacher/qr/{slotId}/current
 *   and refreshes the displayed QR whenever a new token is issued.
 */
@Entity
@Table(name = "qr_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QrToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    // ── The random value encoded in the QR ────────────────────────────────────
    /** UUID encoded in the QR URL — changes every rotation */
    @Column(name = "token_value", nullable = false, unique = true, length = 36)
    private String tokenValue;

    // ── Which class session this token belongs to ──────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false)
    private TimetableSlot slot;

    /** The calendar date this session is running (today's date when generated) */
    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    /** Teacher who started the attendance session */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by", nullable = false)
    private Teacher generatedBy;

    // ── Timing ────────────────────────────────────────────────────────────────
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    // ── Status ────────────────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private QrTokenStatus status = QrTokenStatus.ACTIVE;

    /**
     * How many times this token has been rotated in this session.
     * Useful for debugging / audit.
     */
    @Column(name = "rotation_count", nullable = false)
    @Builder.Default
    private int rotationCount = 0;

    /**
     * Links all rotations of the same session together.
     * Every rotation shares the same sessionId so we can find the "current active"
     * token for a slot+date without scanning all tokens.
     */
    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
