package com.Features.Enrollment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Set;
import java.util.UUID;

/**
 * Bulk promote all ACTIVE students in a given section to a new semester + section.
 *
 * Example: Promote all students in Semester-1 / Section-A
 *          → to Semester-2 / Section-B with new subjects
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkPromoteRequest {

    /** Source: promote all ACTIVE students in this section */
    @NotNull(message = "Source section ID is required")
    private UUID sourceSectionId;

    /** Source semester (narrows down which enrollments to promote) */
    @NotNull(message = "Source semester ID is required")
    private UUID sourceSemesterId;

    /** Target semester */
    @NotNull(message = "Target semester ID is required")
    private UUID targetSemesterId;

    /** Target section */
    @NotNull(message = "Target section ID is required")
    private UUID targetSectionId;

    /** Subjects for the new semester (same for all students in bulk) */
    @NotNull(message = "Subject IDs are required")
    private Set<UUID> subjectIds;

    /** Optional remark applied to all promoted enrollments */
    private String remarks;
}
