package com.Features.Enrollment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Set;
import java.util.UUID;

/**
 * Request body for enrolling a student (single or bulk).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentRequest {

    @NotNull(message = "Student ID is required")
    private UUID studentId;

    @NotNull(message = "Semester ID is required")
    private UUID semesterId;

    @NotNull(message = "Section ID is required")
    private UUID sectionId;

    /** Subject IDs to enroll in. At least one required. */
    @NotNull(message = "At least one subject ID is required")
    private Set<UUID> subjectIds;

    /** Optional admin remark */
    private String remarks;
}
