package com.Features.Attendance.controller;

import com.Features.Attendance.dto.AttendanceRecordResponse;
import com.Features.Attendance.dto.AttendanceReportResponse;
import com.Features.Attendance.dto.QrTokenResponse;
import com.Features.Attendance.service.AttendanceService;
import com.Features.Attendance.service.QrTokenService;
import com.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/attendance")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAttendanceController {

    private final AttendanceService attendanceService;
    private final QrTokenService    qrTokenService;

    // ── Attendance Reports ────────────────────────────────────────────────────

    /**
     * GET /api/admin/attendance/report?slotId=...&date=2026-08-08
     * Full attendance report for one slot on a specific date.
     * Shows each student with PRESENT/ABSENT status + overall %.
     */
    @GetMapping("/report")
    public ResponseEntity<ApiResponse<AttendanceReportResponse>> getReport(
            @RequestParam UUID slotId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        AttendanceReportResponse data = attendanceService.getReport(slotId, date);
        return ResponseEntity.ok(ApiResponse.success("Report retrieved", data));
    }

    /**
     * GET /api/admin/attendance/section?sectionId=...&date=2026-08-08
     * All attendance records for every student in a section on a date.
     * Useful for a daily section-level overview.
     */
    @GetMapping("/section")
    public ResponseEntity<ApiResponse<List<AttendanceRecordResponse>>> getBySection(
            @RequestParam UUID sectionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<AttendanceRecordResponse> data = attendanceService.getBySection(sectionId, date);
        return ResponseEntity.ok(ApiResponse.success("Section attendance retrieved", data));
    }

    /**
     * PUT /api/admin/attendance/override
     * Manually override a student's attendance status for a specific slot+date.
     * Use when a student was wrongly marked absent or missed attendance due to
     * technical issues.
     *
     * Params:
     *   studentId, slotId, date (query params)
     *   status  — PRESENT | ABSENT | LATE (query param)
     *   reason  — optional note
     */
    @PutMapping("/override")
    public ResponseEntity<ApiResponse<AttendanceRecordResponse>> override(
            @RequestParam UUID studentId,
            @RequestParam UUID slotId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String status,
            @RequestParam(required = false) String reason) {
        AttendanceRecordResponse data =
                attendanceService.override(studentId, slotId, date, status, reason);
        return ResponseEntity.ok(ApiResponse.success("Attendance overridden", data));
    }

    // ── QR Management ────────────────────────────────────────────────────────

    /**
     * GET /api/admin/attendance/qr/active
     * All currently ACTIVE QR tokens across all teachers/slots.
     * Admin can see which classes have live attendance sessions.
     */
    @GetMapping("/qr/active")
    public ResponseEntity<ApiResponse<List<QrTokenResponse>>> getActiveQrTokens() {
        List<QrTokenResponse> data = qrTokenService.getAllActiveTokens();
        return ResponseEntity.ok(ApiResponse.success("Active QR tokens retrieved", data));
    }

    /**
     * DELETE /api/admin/attendance/qr/{tokenId}/revoke
     * Immediately revoke any active QR token.
     * Use if a session should be terminated (e.g. class cancelled mid-session).
     */
    @DeleteMapping("/qr/{tokenId}/revoke")
    public ResponseEntity<ApiResponse<Void>> revokeQrToken(@PathVariable UUID tokenId) {
        qrTokenService.revokeToken(tokenId);
        return ResponseEntity.ok(ApiResponse.success("QR token revoked"));
    }
}
