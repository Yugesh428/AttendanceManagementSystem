package com.Features.Timetable.model;

import com.Features.Admin.Classroom.model.Classroom;
import com.Features.Teacher.model.Teacher;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * One-off override for a TimetableSlot on a specific calendar date.
 *
 * Examples of what this covers:
 *   - Teacher is sick on 2026-03-10 → status=CANCELLED
 *   - Room change on 2026-03-12    → status=RESCHEDULED, overrideClassroom=Lab B
 *   - Substitute on 2026-03-15     → status=SUBSTITUTED, substituteTeacher=Ms Ali
 *   - Time shift on 2026-03-20     → status=RESCHEDULED, overrideStartTime=10:00
 *
 * Resolution rule (applied by the service when building a teacher's dashboard):
 *   For each base slot that falls on a given date, check if a TimetableException
 *   exists for (slot, date). If yes → apply the exception. If no → use base slot.
 */
@Entity
@Table(
    name = "timetable_exceptions",
    uniqueConstraints = {
        // Only one exception per slot per date
        @UniqueConstraint(
            name = "uk_slot_date",
            columnNames = {"slot_id", "exception_date"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimetableException {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    // ── Which base slot is overridden ──────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false)
    private TimetableSlot slot;

    // ── On which specific date ─────────────────────────────────────────────────
    @Column(name = "exception_date", nullable = false)
    private LocalDate exceptionDate;

    // ── What changed ──────────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SlotStatus status = SlotStatus.CANCELLED;

    /** Overridden start time (if rescheduled) — null means keep original */
    @Column(name = "override_start_time")
    private LocalTime overrideStartTime;

    /** Overridden end time (if rescheduled) */
    @Column(name = "override_end_time")
    private LocalTime overrideEndTime;

    /** Different room on this date */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "override_classroom_id")
    private Classroom overrideClassroom;

    /** Substitute teacher on this date */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "substitute_teacher_id")
    private Teacher substituteTeacher;

    /** Admin's reason / note for this exception */
    @Column(length = 500)
    private String reason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
