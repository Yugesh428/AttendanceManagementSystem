package com.Features.Enrollment.model;

import com.Features.Admin.Section.Section;
import com.Features.Admin.Semester.Semester;
import com.Features.Admin.Student.model.Student;
import com.Features.Admin.Subject.Subject;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Records which semester, section, and subjects a student is enrolled in.
 *
 * One student can have MULTIPLE enrollment records over time
 * (one per semester they attend).
 *
 * History example:
 *   Enrollment 1 — student=Ali, semester=Sem1, section=A, status=PROMOTED, promotedAt=2025-06-01
 *   Enrollment 2 — student=Ali, semester=Sem2, section=B, status=ACTIVE
 *
 * Active enrollment = the row with status=ACTIVE.
 * Attendance validation uses the ACTIVE enrollment to check:
 *   - Is this student enrolled in this semester?
 *   - Is this student in this section?
 *   - Is this student enrolled in the subject being attended?
 */
@Entity
@Table(
    name = "enrollments",
    uniqueConstraints = {
        // A student can only have one ACTIVE enrollment per semester
        @UniqueConstraint(
            name = "uk_student_semester",
            columnNames = {"student_id", "semester_id"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    // ── Who ───────────────────────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    // ── Where (academic placement) ────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    // ── Which subjects ────────────────────────────────────────────────────────
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "enrollment_subjects",
        joinColumns = @JoinColumn(name = "enrollment_id"),
        inverseJoinColumns = @JoinColumn(name = "subject_id")
    )
    @Builder.Default
    private Set<Subject> subjects = new HashSet<>();

    // ── Status ────────────────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;

    /** Set when admin promotes this student — records the promotion date. */
    @Column(name = "promoted_at")
    private LocalDateTime promotedAt;

    /** Optional note admin can add (e.g. "Promoted due to semester completion") */
    @Column(length = 500)
    private String remarks;

    @CreationTimestamp
    @Column(name = "enrolled_at", updatable = false)
    private LocalDateTime enrolledAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
