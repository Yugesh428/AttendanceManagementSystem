package com.Features.Timetable.model;

import com.Features.Admin.Classroom.model.Classroom;
import com.Features.Admin.Section.Section;
import com.Features.Admin.Subject.Subject;
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
 * One recurring weekly slot in the timetable.
 *
 * Example: every MONDAY 09:00–10:00, Teacher John teaches CS101 in Lab A
 *          effective from 2026-01-01 to 2026-06-30
 *
 * This is the BASE schedule. For one-off changes (substitutions, cancellations,
 * room swaps) on a specific date, a TimetableException is created that
 * overrides this slot on that date only.
 */
@Entity
@Table(
    name = "timetable_slots",
    uniqueConstraints = {
        // Same teacher cannot have two slots overlapping on the same day/time
        @UniqueConstraint(
            name = "uk_teacher_day_start",
            columnNames = {"teacher_id", "day_of_week", "start_time", "effective_from"}
        ),
        // Same classroom cannot be double-booked
        @UniqueConstraint(
            name = "uk_classroom_day_start",
            columnNames = {"classroom_id", "day_of_week", "start_time", "effective_from"}
        ),
        // Same section cannot have two classes at the same time
        @UniqueConstraint(
            name = "uk_section_day_start",
            columnNames = {"section_id", "day_of_week", "start_time", "effective_from"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimetableSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    // ── Who teaches ────────────────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    // ── What subject ───────────────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    // ── Where ──────────────────────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    // ── Which section attends this slot ───────────────────────────────────────
    /**
     * The student section assigned to this slot.
     * Used during attendance scan to verify the scanning student
     * actually belongs to this class (prevents cross-section cheating).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    // ── When (recurring weekly) ────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    // ── Effective date range ───────────────────────────────────────────────────
    /**
     * This slot is active from effectiveFrom to effectiveTo (inclusive).
     * Use these to represent semester/term boundaries.
     * effectiveTo = null means "open-ended / until further notice".
     */
    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    // ── Optional label ─────────────────────────────────────────────────────────
    @Column(length = 255)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
