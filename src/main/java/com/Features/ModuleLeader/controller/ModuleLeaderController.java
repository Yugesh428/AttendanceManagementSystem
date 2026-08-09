package com.Features.ModuleLeader.controller;

import com.Features.Attendance.dto.AttendanceReportResponse;
import com.Features.Attendance.dto.SlotHistoryResponse;
import com.Features.ModuleLeader.dto.ModuleLeaderResponse;
import com.Features.ModuleLeader.dto.StudentAttendanceSummaryDTO;
import com.Features.ModuleLeader.dto.TeacherSummaryDTO;
import com.Features.ModuleLeader.service.ModuleLeaderService;
import com.Features.Timetable.dto.TimetableSlotDTO;
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
@RequestMapping("/api/module-leader")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MODULE_LEADER')")
public class ModuleLeaderController {

    private final ModuleLeaderService moduleLeaderService;

    /**
     * GET /api/module-leader/me
     * Own profile: name, email, assigned subject.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ModuleLeaderResponse>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved",
                moduleLeaderService.getMyProfile(userDetails.getUsername())));
    }

    /**
     * GET /api/module-leader/slots
     * All timetable slots that teach this module leader's subject.
     * Shows every section, teacher, classroom and time the subject runs.
     */
    @GetMapping("/slots")
    public ResponseEntity<ApiResponse<List<TimetableSlotDTO>>> getSlots(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<TimetableSlotDTO> data =
                moduleLeaderService.getMySlotsSubject(userDetails.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success(data.size() + " slot(s) found for your subject", data));
    }

    /**
     * GET /api/module-leader/attendance/report?slotId=...&date=2026-08-08
     * Attendance report for one slot on a specific date.
     * Shows each student PRESENT/ABSENT + overall % for that session.
     * Module leader can only access slots that belong to their subject.
     */
    @GetMapping("/attendance/report")
    public ResponseEntity<ApiResponse<AttendanceReportResponse>> getReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam UUID slotId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        AttendanceReportResponse data =
                moduleLeaderService.getSlotReport(userDetails.getUsername(), slotId, date);
        return ResponseEntity.ok(ApiResponse.success("Attendance report retrieved", data));
    }

    /**
     * GET /api/module-leader/attendance/slots/{slotId}/history
     * Full attendance history for one slot — all past session dates.
     * Each date shows present count, absent count, and %.
     * Useful for tracking trends over the semester.
     * Module leader can only access slots that belong to their subject.
     */
    @GetMapping("/attendance/slots/{slotId}/history")
    public ResponseEntity<ApiResponse<SlotHistoryResponse>> getSlotHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID slotId) {
        SlotHistoryResponse data =
                moduleLeaderService.getSlotHistory(userDetails.getUsername(), slotId);
        return ResponseEntity.ok(ApiResponse.success("Slot history retrieved", data));
    }

    /**
     * GET /api/module-leader/students
     * All students enrolled in the module leader's subject (across all sections)
     * with their individual attendance percentage.
     * Sorted alphabetically by student name.
     */
    @GetMapping("/students")
    public ResponseEntity<ApiResponse<List<StudentAttendanceSummaryDTO>>> getStudentSummaries(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<StudentAttendanceSummaryDTO> data =
                moduleLeaderService.getStudentSummaries(userDetails.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success(data.size() + " student(s) enrolled in your subject", data));
    }

    /**
     * GET /api/module-leader/teachers
     * All teachers who have timetable slots for this module leader's subject.
     * Shows name, designation, department, and how many slots each teacher has.
     */
    @GetMapping("/teachers")
    public ResponseEntity<ApiResponse<List<TeacherSummaryDTO>>> getTeachers(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<TeacherSummaryDTO> data =
                moduleLeaderService.getTeachersForSubject(userDetails.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success(data.size() + " teacher(s) teaching your subject", data));
    }
}
