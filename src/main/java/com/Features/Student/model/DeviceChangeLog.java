package com.Features.Student.model;

import com.Features.Admin.Student.model.Student;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Recorded every time a student logs in from a device that does NOT match
 * the registered phone deviceId in their StudentAccount.
 *
 * Soft binding — login is allowed, but admin is alerted and can review.
 * Admin can acknowledge (flag as OK) or reset the device binding.
 */
@Entity
@Table(name = "device_change_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceChangeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "old_device_id", length = 255)
    private String oldDeviceId;

    @Column(name = "new_device_id", length = 255, nullable = false)
    private String newDeviceId;

    /** Admin has reviewed and acknowledged this change */
    @Builder.Default
    @Column(name = "acknowledged", nullable = false)
    private boolean acknowledged = false;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @CreationTimestamp
    @Column(name = "detected_at", updatable = false)
    private LocalDateTime detectedAt;
}
