package com.Features.FacultyAttendance.controller;

import com.Features.FacultyAttendance.dto.FacultyAttendanceRecordResponse;
import com.Features.FacultyAttendance.dto.FacultyAttendanceScanRequest;
import com.Features.FacultyAttendance.dto.FacultyDailyQrResponse;
import com.Features.FacultyAttendance.service.FacultyAttendanceService;
import com.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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

@RestController
@RequestMapping("/api/faculty/attendance")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class FacultyAttendanceController {

    private final FacultyAttendanceService service;

    /**
     * GET /api/faculty/attendance/qr/today
     * Faculty views today's QR before scanning.
     * Returns the tokenValue and qrUrl.
     *
     * Errors: 404 — admin hasn't generated today's QR yet
     */
    @GetMapping("/qr/today")
    public ResponseEntity<ApiResponse<FacultyDailyQrResponse>> getTodayQr() {
        FacultyDailyQrResponse data = service.getTodayQr();
        return ResponseEntity.ok(ApiResponse.success("Today's QR retrieved", data));
    }

    /**
     * POST /api/faculty/attendance/scan
     * Faculty scans the daily QR to mark attendance.
     *
     * Validations:
     *   1. QR exists and is not expired/revoked
     *   2. Faculty is on college WiFi (IP range check)
     *   3. No duplicate attendance for today
     *
     * Body: { "qrToken": "<tokenValue>" }
     *
     * The server reads the real client IP automatically from
     * X-Forwarded-For or RemoteAddr — no need to send IP manually.
     *
     * Errors:
     *   403 — not on college WiFi
     *   410 — QR expired or revoked
     *   409 — already marked today
     */
    @PostMapping("/scan")
    public ResponseEntity<ApiResponse<FacultyAttendanceRecordResponse>> scan(
            @Valid @RequestBody FacultyAttendanceScanRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {

        String clientIp = extractClientIp(httpRequest);
        FacultyAttendanceRecordResponse data =
                service.scan(request.getQrToken(), userDetails.getUsername(), clientIp);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Attendance marked successfully", data));
    }

    /**
     * GET /api/faculty/attendance/history?from=2026-08-01&to=2026-08-31
     * Faculty views their own attendance history for a date range.
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<FacultyAttendanceRecordResponse>>> getMyHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        // Find faculty ID via teacher → faculty relationship
        // The UserDetails username = faculty email (same as teacher login email)
        // We reuse getFacultyHistory with the faculty found by email in the service
        // For simplicity pass the email; service resolves facultyId
        // We need a separate method — see note below
        // Using the faculty email path via override with a helper
        List<FacultyAttendanceRecordResponse> data =
                service.getMyHistory(userDetails.getUsername(), from, to);
        return ResponseEntity.ok(ApiResponse.success("Attendance history retrieved", data));
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    /**
     * Extracts the real client IP, handling reverse proxies.
     * Checks X-Forwarded-For first, falls back to RemoteAddr.
     */
    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // X-Forwarded-For can be comma-separated list — take the first (original client)
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
