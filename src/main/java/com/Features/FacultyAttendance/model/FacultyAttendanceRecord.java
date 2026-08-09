package com.Features.FacultyAttendance.model;

import com.Features.Admin.faculty.Faculty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * One attendance record per faculty per date.
 * Created when faculty scans the daily QR on college WiFi.
 */
@Entity
@Table(
    name = "faculty_attendance_records",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_faculty_attendance_date",
            columnNames = {"faculty_id", "attendance_date"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacultyAttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private FacultyAttendanceStatus status = FacultyAttendanceStatus.PRESENT;

    /** IP address from which the QR was scanned — for audit */
    @Column(name = "scanned_from_ip", length = 45)
    private String scannedFromIp;

    /** The daily QR that was scanned */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qr_id")
    private FacultyDailyQr qr;

    @CreationTimestamp
    @Column(name = "marked_at", updatable = false)
    private LocalDateTime markedAt;
}
