package com.Features.Enrollment.model;

/**
 * Lifecycle of a student's enrollment record.
 *
 * ACTIVE    — student is currently attending this semester/section
 * PROMOTED  — student was promoted; a new ACTIVE enrollment was created
 * DROPPED   — student dropped out or was removed from this enrollment
 */
public enum EnrollmentStatus {
    ACTIVE,
    PROMOTED,
    DROPPED
}
