package com.Features.Enrollment.controller;

import com.Features.Enrollment.dto.BulkPromoteRequest;
import com.Features.Enrollment.dto.EnrollmentRequest;
import com.Features.Enrollment.dto.EnrollmentResponse;
import com.Features.Enrollment.dto.PromoteRequest;
import com.Features.Enrollment.service.EnrollmentService;
import com.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/enrollments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    // ── Enroll ────────────────────────────────────────────────────────────────

    /**
     * POST /api/admin/enrollments
     * Enroll a student into a semester + section + subjects.
     * Sends a welcome email to the student automatically.
     *
     * Body:
     * {
     *   "studentId": "<uuid>",
     *   "semesterId": "<uuid>",
     *   "sectionId": "<uuid>",
     *   "subjectIds": ["<uuid>", "<uuid>"],
     *   "remarks": "optional"
     * }
     *
     * Errors:
     *   404 — student / semester / section / subject not found
     *   409 — student already enrolled in this semester
     */
    @PostMapping
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enroll(
            @Valid @RequestBody EnrollmentRequest request) {
        EnrollmentResponse data = enrollmentService.enroll(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Student enrolled successfully", data));
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    /** GET /api/admin/enrollments/{id} — get one enrollment */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.success("Enrollment retrieved", enrollmentService.getById(id)));
    }

    /**
     * GET /api/admin/enrollments/student/{studentId}/active
     * Get the student's current (ACTIVE) enrollment.
     */
    @GetMapping("/student/{studentId}/active")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> getActiveByStudent(
            @PathVariable UUID studentId) {
        return ResponseEntity.ok(
                ApiResponse.success("Active enrollment retrieved",
                        enrollmentService.getActiveEnrollmentByStudent(studentId)));
    }

    /**
     * GET /api/admin/enrollments/student/{studentId}/history
     * Full enrollment history for a student (all semesters).
     */
    @GetMapping("/student/{studentId}/history")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getHistory(
            @PathVariable UUID studentId) {
        return ResponseEntity.ok(
                ApiResponse.success("Enrollment history retrieved",
                        enrollmentService.getHistoryByStudent(studentId)));
    }

    /**
     * GET /api/admin/enrollments/section/{sectionId}
     * All ACTIVE students in a section.
     */
    @GetMapping("/section/{sectionId}")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getBySection(
            @PathVariable UUID sectionId) {
        return ResponseEntity.ok(
                ApiResponse.success("Section enrollments retrieved",
                        enrollmentService.getActiveBySection(sectionId)));
    }

    /**
     * GET /api/admin/enrollments/semester/{semesterId}
     * All ACTIVE students in a semester.
     */
    @GetMapping("/semester/{semesterId}")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getBySemester(
            @PathVariable UUID semesterId) {
        return ResponseEntity.ok(
                ApiResponse.success("Semester enrollments retrieved",
                        enrollmentService.getActiveBySemester(semesterId)));
    }

    // ── Promote ───────────────────────────────────────────────────────────────

    /**
     * POST /api/admin/enrollments/{id}/promote
     * Promote one student to a new semester + section.
     * Marks current enrollment as PROMOTED, creates new ACTIVE enrollment.
     *
     * Body:
     * {
     *   "targetSemesterId": "<uuid>",
     *   "targetSectionId":  "<uuid>",
     *   "subjectIds": ["<uuid>"],
     *   "remarks": "optional"
     * }
     */
    @PostMapping("/{id}/promote")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> promote(
            @PathVariable UUID id,
            @Valid @RequestBody PromoteRequest request) {
        EnrollmentResponse data = enrollmentService.promoteStudent(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Student promoted successfully", data));
    }

    /**
     * POST /api/admin/enrollments/bulk-promote
     * Promote ALL ACTIVE students in a source section/semester at once.
     *
     * Body:
     * {
     *   "sourceSectionId":  "<uuid>",
     *   "sourceSemesterId": "<uuid>",
     *   "targetSemesterId": "<uuid>",
     *   "targetSectionId":  "<uuid>",
     *   "subjectIds": ["<uuid>"],
     *   "remarks": "optional"
     * }
     *
     * Returns list of new ACTIVE enrollments created.
     * Students already enrolled in the target semester are skipped (not an error).
     */
    @PostMapping("/bulk-promote")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> bulkPromote(
            @Valid @RequestBody BulkPromoteRequest request) {
        List<EnrollmentResponse> data = enrollmentService.bulkPromote(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201,
                        data.size() + " student(s) promoted successfully", data));
    }

    // ── Drop ──────────────────────────────────────────────────────────────────

    /**
     * PUT /api/admin/enrollments/{id}/drop
     * Mark a student as DROPPED from their current enrollment.
     * Pass reason as a query param: ?reason=Failed+exams
     */
    @PutMapping("/{id}/drop")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> drop(
            @PathVariable UUID id,
            @RequestParam(required = false) String reason) {
        EnrollmentResponse data = enrollmentService.dropStudent(id, reason);
        return ResponseEntity.ok(
                ApiResponse.success("Student dropped from enrollment", data));
    }

    // ── Update subjects ───────────────────────────────────────────────────────

    /**
     * PUT /api/admin/enrollments/{id}/subjects
     * Replace the enrolled subjects for an ACTIVE enrollment.
     * Body: ["<uuid>", "<uuid>"]   (array of subject IDs)
     */
    @PutMapping("/{id}/subjects")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> updateSubjects(
            @PathVariable UUID id,
            @RequestBody Set<UUID> subjectIds) {
        EnrollmentResponse data = enrollmentService.updateSubjects(id, subjectIds);
        return ResponseEntity.ok(
                ApiResponse.success("Subjects updated successfully", data));
    }
}
