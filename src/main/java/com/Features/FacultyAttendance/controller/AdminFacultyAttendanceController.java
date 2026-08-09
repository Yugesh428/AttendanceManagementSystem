package com.Features.FacultyAttendance.controller;

import com.Features.FacultyAttendance.dto.*;
import com.Features.FacultyAttendance.service.FacultyAttendanceService;
import com.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/faculty-attendance")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminFacultyAttendanceController {

    private final FacultyAttendanceService service;

    /**
     * POST /api/admin/faculty-attendance/qr/generate
     * Generate the daily QR for today (or a specific date).
     * All faculty scan this same QR once per day.
     *
     * Optional query param: ?date=2026-08-08 (defaults to today)
     * Returns the QR token value + URL to display on screen.
     * Calling again on the same date returns the existing QR (idempotent).
     */
    @PostMapping("/qr/generate")
    public ResponseEntity<ApiResponse<FacultyDailyQrResponse>> generateQr(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserDetails userDetails) {
        LocalDate target = date != null ? date : LocalDate.now();
        FacultyDailyQrResponse data = service.generateDailyQr(target, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Daily QR generated for " + target, data));
    }

    /**
     * GET /api/admin/faculty-attendance/qr/today
     * Get the currently active daily QR.
     * Admin displays this on a screen or projector for faculty to scan.
     */
    @GetMapping("/qr/today")
    public ResponseEntity<ApiResponse<FacultyDailyQrResponse>> getTodayQr() {
        FacultyDailyQrResponse data = service.getTodayQr();
        return ResponseEntity.ok(ApiResponse.success("Today's faculty QR retrieved", data));
    }

    /**
     * DELETE /api/admin/faculty-attendance/qr/{qrId}/revoke
     * Revoke the QR immediately — no more scans accepted.
     */
    @DeleteMapping("/qr/{qrId}/revoke")
    public ResponseEntity<ApiResponse<Void>> revokeQr(@PathVariable UUID qrId) {
        service.revokeQr(qrId);
        return ResponseEntity.ok(ApiResponse.success("Faculty QR revoked"));
    }

    /**
     * GET /api/admin/faculty-attendance/report?date=2026-08-08
     * Full attendance report for all faculty on a specific date.
     * Shows each faculty member with PRESENT/ABSENT status + overall %.
     */
    @GetMapping("/report")
    public ResponseEntity<ApiResponse<FacultyAttendanceReportResponse>> getDailyReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        FacultyAttendanceReportResponse data = service.getDailyReport(date);
        return ResponseEntity.ok(ApiResponse.success("Faculty attendance report retrieved", data));
    }

    /**
     * GET /api/admin/faculty-attendance/report/department?departmentId=...&date=2026-08-08
     * Attendance report filtered by department.
     */
    @GetMapping("/report/department")
    public ResponseEntity<ApiResponse<FacultyAttendanceReportResponse>> getDepartmentReport(
            @RequestParam UUID departmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        FacultyAttendanceReportResponse data = service.getDepartmentReport(departmentId, date);
        return ResponseEntity.ok(
                ApiResponse.success("Department attendance report retrieved", data));
    }

    /**
     * GET /api/admin/faculty-attendance/history/{facultyId}?from=...&to=...
     * Full attendance history for one faculty member.
     */
    @GetMapping("/history/{facultyId}")
    public ResponseEntity<ApiResponse<List<FacultyAttendanceRecordResponse>>> getFacultyHistory(
            @PathVariable UUID facultyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<FacultyAttendanceRecordResponse> data = service.getFacultyHistory(facultyId, from, to);
        return ResponseEntity.ok(ApiResponse.success("Faculty history retrieved", data));
    }

    /**
     * PUT /api/admin/faculty-attendance/override
     * Manually override a faculty member's attendance.
     * status: PRESENT | ABSENT | LATE | LEAVE
     *
     * Params: facultyId, date, status, reason (optional)
     */
    @PutMapping("/override")
    public ResponseEntity<ApiResponse<FacultyAttendanceRecordResponse>> override(
            @RequestParam UUID facultyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String status,
            @RequestParam(required = false) String reason) {
        FacultyAttendanceRecordResponse data = service.override(facultyId, date, status, reason);
        return ResponseEntity.ok(ApiResponse.success("Faculty attendance overridden", data));
    }
}
