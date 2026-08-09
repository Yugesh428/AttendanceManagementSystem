package com.Features.Teacher.controller;

import com.Features.Attendance.dto.AttendanceReportResponse;
import com.Features.Attendance.dto.SlotHistoryResponse;
import com.Features.Attendance.dto.SlotStudentResponse;
import com.Features.Attendance.service.AttendanceService;
import com.Features.Teacher.dto.TeacherDTO;
import com.Features.Teacher.service.TeacherService;
import com.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherController {

    private final TeacherService    teacherService;
    private final AttendanceService attendanceService;

    // ── Profile ───────────────────────────────────────────────────────────────

    /**
     * GET /api/teacher/me
     * Own profile: name, email, faculty details, department, designation.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<TeacherDTO>> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        TeacherDTO data = teacherService.getMyProfile(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", data));
    }

    // ── Attendance report (single session) ───────────────────────────────────

    /**
     * GET /api/teacher/attendance/report?slotId=...&date=2026-08-08
     * Attendance report for one class session on a specific date.
     * Shows each student PRESENT/ABSENT + total % for that session.
     *
     * Get slotId from GET /api/teacher/timetable/today
     */
    @GetMapping("/attendance/report")
    public ResponseEntity<ApiResponse<AttendanceReportResponse>> getAttendanceReport(
            @RequestParam UUID slotId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        AttendanceReportResponse data = attendanceService.getReport(slotId, date);
        return ResponseEntity.ok(ApiResponse.success("Attendance report retrieved", data));
    }

    // ── Students in a slot ────────────────────────────────────────────────────

    /**
     * GET /api/teacher/slots/{slotId}/students
     * Lists all students enrolled in a slot's section who take this subject.
     * Teacher uses this before starting a session to see who is expected.
     *
     * Response: sorted alphabetically by first name.
     * Each entry: studentId, firstName, lastName, email, phone
     */
    @GetMapping("/slots/{slotId}/students")
    public ResponseEntity<ApiResponse<List<SlotStudentResponse>>> getStudentsForSlot(
            @PathVariable UUID slotId) {
        List<SlotStudentResponse> data = attendanceService.getStudentsForSlot(slotId);
        return ResponseEntity.ok(
                ApiResponse.success(data.size() + " student(s) enrolled in this slot", data));
    }

    // ── Slot history (all past sessions) ─────────────────────────────────────

    /**
     * GET /api/teacher/slots/{slotId}/history
     * Full attendance history for a slot across ALL past session dates.
     *
     * Response includes:
     *   - sessionDates: list of all dates this slot has run
     *   - sessions: each date with present/absent breakdown + student list
     *   - Sorted newest date first
     *
     * Teacher uses this to track attendance trends over the semester.
     */
    @GetMapping("/slots/{slotId}/history")
    public ResponseEntity<ApiResponse<SlotHistoryResponse>> getSlotHistory(
            @PathVariable UUID slotId) {
        SlotHistoryResponse data = attendanceService.getSlotHistory(slotId);
        return ResponseEntity.ok(
                ApiResponse.success("Slot attendance history retrieved", data));
    }
}
