package com.Features.Enrollment.dto;

import com.Features.Enrollment.model.EnrollmentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EnrollmentResponse {

    private UUID id;

    // ── Student ───────────────────────────────────────────────────────────────
    private UUID studentId;
    private String studentName;
    private String studentEmail;

    // ── Placement ─────────────────────────────────────────────────────────────
    private UUID semesterId;
    private String semesterName;

    private UUID sectionId;
    private String sectionName;

    // ── Subjects ──────────────────────────────────────────────────────────────
    private Set<SubjectInfo> subjects;

    // ── Status ────────────────────────────────────────────────────────────────
    private EnrollmentStatus status;
    private String remarks;
    private LocalDateTime promotedAt;
    private LocalDateTime enrolledAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectInfo {
        private UUID id;
        private String subjectName;
        private String subjectCode;
    }
}
