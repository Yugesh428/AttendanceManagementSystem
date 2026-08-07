package com.Features.Teacher.model;

import com.Features.Admin.faculty.Faculty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A Teacher is a Faculty member who has been assigned a login account
 * by the Admin so they can mark attendance for their students.
 *
 * Flow:
 *   Admin registers Faculty  →  Admin promotes Faculty to Teacher
 *   →  TeacherAccount auto-created  →  credentials sent to teacher
 *   →  Teacher logs in at POST /api/auth/teacher/login
 *   →  Teacher marks attendance at POST /api/teacher/attendance
 */
@Entity
@Table(name = "teachers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    /**
     * Every teacher must be backed by a Faculty record.
     * Admin picks which faculty member becomes a teacher.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id", nullable = false, unique = true)
    private Faculty faculty;

    /** Extra teacher-specific notes (optional) */
    @Column(length = 500)
    private String notes;

    @Builder.Default
    private boolean active = true;

    /** Login account — created automatically when teacher is registered */
    @OneToOne(mappedBy = "teacher", cascade = CascadeType.ALL,
              fetch = FetchType.LAZY, orphanRemoval = true)
    private TeacherAccount account;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
