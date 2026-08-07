package com.Features.Enrollment.service;

import com.Features.Enrollment.dto.BulkPromoteRequest;
import com.Features.Enrollment.dto.EnrollmentRequest;
import com.Features.Enrollment.dto.EnrollmentResponse;
import com.Features.Enrollment.dto.PromoteRequest;

import java.util.List;
import java.util.UUID;

public interface EnrollmentService {

    // ── Enroll ───────────────────────────────────────────────────────────────
    EnrollmentResponse enroll(EnrollmentRequest request);

    // ── Read ─────────────────────────────────────────────────────────────────
    EnrollmentResponse getById(UUID id);

    List<EnrollmentResponse> getHistoryByStudent(UUID studentId);

    List<EnrollmentResponse> getActiveBySection(UUID sectionId);

    List<EnrollmentResponse> getActiveBySemester(UUID semesterId);

    EnrollmentResponse getActiveEnrollmentByStudent(UUID studentId);

    // ── Promote ──────────────────────────────────────────────────────────────
    EnrollmentResponse promoteStudent(UUID enrollmentId, PromoteRequest request);

    List<EnrollmentResponse> bulkPromote(BulkPromoteRequest request);

    // ── Drop ─────────────────────────────────────────────────────────────────
    EnrollmentResponse dropStudent(UUID enrollmentId, String reason);

    // ── Update subjects ───────────────────────────────────────────────────────
    EnrollmentResponse updateSubjects(UUID enrollmentId, java.util.Set<UUID> subjectIds);
}
