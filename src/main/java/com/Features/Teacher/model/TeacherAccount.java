package com.Features.Teacher.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Login credentials for a Teacher.
 * Auto-created when Admin registers a Faculty member as a Teacher.
 *
 * Default password = first4(firstName, lowercase) + "@" + last4(phone)
 * e.g. firstName="John", phone="9876543210"  →  "john@3210"
 *
 * The plain-text password is returned ONCE in the API response
 * (TeacherDTO.generatedPassword). It is BCrypt-hashed before storage
 * and never returned again after the creation response.
 */
@Entity
@Table(name = "teacher_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    /** Login username = faculty email */
    @Column(nullable = false, unique = true, length = 100)
    private String username;

    /** BCrypt-hashed password */
    @Column(nullable = false)
    private String password;

    @Builder.Default
    private boolean active = true;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false, unique = true)
    private Teacher teacher;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
