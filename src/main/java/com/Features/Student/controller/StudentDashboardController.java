package com.Features.Student.controller;

import com.Features.Admin.Student.DTO.StudentDTO;
import com.Features.Admin.Student.model.Student;
import com.Features.Admin.Student.repository.StudentRepository;
import com.Features.Enrollment.dto.EnrollmentResponse;
import com.Features.Enrollment.service.EnrollmentService;
import com.Features.Timetable.dto.ResolvedSlotDTO;
import com.Features.Timetable.service.TimetableService;
import com.common.ApiResponse;
import com.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentDashboardController {

    private final StudentRepository studentRepository;
    private final EnrollmentService enrollmentService;
    private final TimetableService  timetableService;

    /**
     * GET /api/student/me
     * Returns the logged-in student's profile.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<StudentDTO>> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        Student student = findStudent(userDetails.getUsername());

        StudentDTO dto = StudentDTO.builder()
                .id(student.getId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .email(student.getEmail())
                .phone(student.getPhone())
                .dateOfBirth(student.getDateOfBirth())
                .gender(student.getGender())
                .fatherName(student.getFatherName())
                .fatherPhone(student.getFatherPhone())
                .motherName(student.getMotherName())
                .motherPhone(student.getMotherPhone())
                .guardianName(student.getGuardianName())
                .guardianPhone(student.getGuardianPhone())
                .createdAt(student.getCreatedAt())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Profile retrieved", dto));
    }

    /**
     * GET /api/student/enrollment
     * Returns the student's current (ACTIVE) enrollment —
     * semester, section, and enrolled subjects.
     */
    @GetMapping("/enrollment")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> getMyEnrollment(
            @AuthenticationPrincipal UserDetails userDetails) {

        Student student = findStudent(userDetails.getUsername());
        EnrollmentResponse data = enrollmentService
                .getActiveEnrollmentByStudent(student.getId());

        return ResponseEntity.ok(ApiResponse.success("Active enrollment retrieved", data));
    }

    /**
     * GET /api/student/timetable/today
     * Student sees today's classes for their section.
     * Shows ACTIVE / CANCELLED / RESCHEDULED status.
     * The slotId from each entry is used in the QR scan URL.
     */
    @GetMapping("/timetable/today")
    public ResponseEntity<ApiResponse<List<ResolvedSlotDTO>>> getTodayTimetable(
            @AuthenticationPrincipal UserDetails userDetails) {

        // Students see timetable via the enrollment-linked section.
        // The TimetableService currently resolves by teacher email.
        // For student view: we return the section's timetable for today.
        // We reuse the teacher's today endpoint scoped to the student's email
        // (TimetableService.getMyTodaySchedule works on teacher email only — 
        //  we provide a dedicated section-based query via the service below).
        List<ResolvedSlotDTO> data = timetableService
                .getStudentTodaySchedule(userDetails.getUsername());

        return ResponseEntity.ok(
                ApiResponse.success("Today's timetable retrieved", data));
    }

    /**
     * GET /api/student/timetable/week?weekStart=2026-08-10
     * Student sees their full week schedule.
     */
    @GetMapping("/timetable/week")
    public ResponseEntity<ApiResponse<List<ResolvedSlotDTO>>> getWeekTimetable(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {

        List<ResolvedSlotDTO> data = timetableService
                .getStudentWeekSchedule(userDetails.getUsername(), weekStart);

        return ResponseEntity.ok(
                ApiResponse.success("Week timetable retrieved", data));
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private Student findStudent(String email) {
        return studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "email", email));
    }
}
