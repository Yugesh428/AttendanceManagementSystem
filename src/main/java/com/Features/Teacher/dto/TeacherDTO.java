package com.Features.Teacher.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)   // hides null fields (e.g. generatedPassword on updates)
public class TeacherDTO {

    private UUID id;

    // ── Faculty reference ──────────────────────────────────────────────────────
    @NotNull(message = "Faculty ID is required")
    private UUID facultyId;

    // ── Faculty details (populated in responses) ───────────────────────────────
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private String phone;
    private String designation;
    private String departmentName;
    private LocalDate joiningDate;

    // ── Teacher-specific ───────────────────────────────────────────────────────
    private String notes;
    private boolean active;

    /**
     * Plain-text generated password — shown ONCE in the creation response.
     * Admin must share this with the teacher.
     * Never stored in plain text and excluded from all other responses.
     */
    private String generatedPassword;

    private LocalDateTime createdAt;
}
