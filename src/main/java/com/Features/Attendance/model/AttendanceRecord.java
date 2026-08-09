package com.Features.Attendance.model;

import com.Features.Admin.Student.model.Student;
import com.Features.Timetable.model.TimetableSlot;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * One attendance mark per student per class period per date.
 *
 * A student can only have ONE record per (student, slot, date).
 * Status defaults to PRESENT on successful QR scan.
 * Absent records are generated automatically at session end for
 * all enrolled students who did NOT scan.
 */
@Entity
@Table(
    name = "attendance_records",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_student_slot_date",
            columnNames = {"student_id", "slot_id", "attendance_date"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    // ── Who ───────────────────────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    // ── Which class ───────────────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false)
    private TimetableSlot slot;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    // ── Status ────────────────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private AttendanceStatus status = AttendanceStatus.PRESENT;

    // ── Proof of presence ─────────────────────────────────────────────────────
    /** Device fingerprint used at scan time */
    @Column(name = "device_id", length = 255)
    private String deviceId;

    /** Beacon UUID detected at scan time — proves physical presence */
    @Column(name = "beacon_id", length = 100)
    private String beaconId;

    /** The QR token that was scanned */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qr_token_id")
    private QrToken qrToken;

    // ── Audit ─────────────────────────────────────────────────────────────────
    @CreationTimestamp
    @Column(name = "marked_at", updatable = false)
    private LocalDateTime markedAt;
}
