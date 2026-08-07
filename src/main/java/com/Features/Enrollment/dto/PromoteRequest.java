package com.Features.Enrollment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Set;
import java.util.UUID;

/**
 * Request body for promoting a student to a new semester + section.
 * Used for both single and bulk promotion.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromoteRequest {

    /** New semester to promote into */
    @NotNull(message = "Target semester ID is required")
    private UUID targetSemesterId;

    /** New section in the target semester */
    @NotNull(message = "Target section ID is required")
    private UUID targetSectionId;

    /** Subjects for the new semester (can differ from previous semester) */
    @NotNull(message = "Subject IDs are required")
    private Set<UUID> subjectIds;

    /** Optional remark (e.g. "Promoted after passing final exams") */
    private String remarks;
}
