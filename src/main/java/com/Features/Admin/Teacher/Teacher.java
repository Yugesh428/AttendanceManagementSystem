package com.Features.Admin.Teacher;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

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
    private UUID id;

    // Basic Info
    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 15, unique = true)
    private String phoneNumber;

    // Professional Info
    @Column(length = 100)
    private String qualification;   // e.g. M.Sc, B.Ed

    @Column(length = 100)
    private String specialization;  // e.g. Mathematics, Computer Science

    @Column(length = 100)
    private String designation;     // e.g. Lecturer, Assistant Professor

    private Integer experienceYears;

    // Personal Info
    private LocalDate dateOfBirth;

    @Column(length = 10)
    private String gender;

    @Column(length = 255)
    private String address;

    // System Fields
    @Column(nullable = false)
    private Boolean isActive = true;

    private LocalDate joiningDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Auto timestamps
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        joiningDate = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}