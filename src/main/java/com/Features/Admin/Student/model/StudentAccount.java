package com.Features.Admin.Student.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Login credentials for a Student.
 * Created automatically when a Student is registered (manually or via Excel import).
 *
 * Default password = first 4 chars of firstName (lowercase) + "@" + last 4 digits of phone.
 * e.g. firstName="John", phone="9876543210"  →  password = "john@3210"
 * If phone is absent, password defaults to "Student@" + first 6 chars of id.
 *
 * The raw (plain-text) generated password is returned ONCE in the API response
 * inside StudentDTO.generatedPassword — it is never stored plain-text in the DB.
 */
@Entity
@Table(name = "student_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    /** Login username = student email */
    @Column(nullable = false, unique = true, length = 100)
    private String username;

    /** BCrypt-hashed password */
    @Column(nullable = false)
    private String password;

    @Builder.Default
    private boolean active = true;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false, unique = true)
    private Student student;

    // ── Device registration (phone-first attendance) ───────────────────────────
    /**
     * Unique device fingerprint captured on the student's FIRST login.
     * Must come from a mobile browser (validated via User-Agent).
     * Sent as X-Device-Id header on every subsequent request.
     * Attendance scan is rejected if the incoming deviceId doesn't match this.
     */
    @Column(name = "device_id", length = 255)
    private String deviceId;

    /**
     * Timestamp of when the phone was first registered.
     * Null means student has never logged in from phone yet.
     */
    @Column(name = "device_registered_at")
    private LocalDateTime deviceRegisteredAt;

    /**
     * True once the student has completed their first mobile login.
     * Laptop login is blocked until this is true.
     */
    @Builder.Default
    @Column(name = "phone_registered", nullable = false)
    private boolean phoneRegistered = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
