package com.Features.Admin.Student.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)   // hide null fields (e.g. generatedPassword on updates)
public class StudentDTO {

    private UUID id;

    // ── Personal ───────────────────────────────────────────────────────────────
    @NotBlank(message = "First name is required")
    private String firstName;

    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String phone;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    private String gender;

    // ── Family ─────────────────────────────────────────────────────────────────
    private String fatherName;
    private String fatherPhone;
    private String motherName;
    private String motherPhone;
    private String guardianName;
    private String guardianPhone;

    // ── Account info — only populated on creation, never on updates/lists ──────
    /**
     * The plain-text password generated when this student account was created.
     * Shown ONCE in the creation/import response. Never stored in the database.
     * The admin must share this with the student.
     */
    private String generatedPassword;

    private LocalDateTime createdAt;
}
