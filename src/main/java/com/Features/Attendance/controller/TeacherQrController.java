package com.Features.Attendance.controller;

import com.Features.Attendance.dto.QrTokenResponse;
import com.Features.Attendance.service.AttendanceService;
import com.Features.Attendance.service.QrTokenService;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/teacher/qr")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherQrController {

    private final QrTokenService    qrTokenService;
    private final AttendanceService attendanceService;

    /**
     * POST /api/teacher/qr/{slotId}/start
     * Teacher starts an attendance session for a slot.
     * Generates the first dynamic QR token.
     *
     * Returns: QR token value + expiresAt + qrUrl + secondsUntilExpiry
     * Errors:
     *   403 — you are not the teacher for this slot
     *   409 — session already active for this slot today
     */
    @PostMapping("/{slotId}/start")
    public ResponseEntity<ApiResponse<QrTokenResponse>> startSession(
            @PathVariable UUID slotId,
            @AuthenticationPrincipal UserDetails userDetails) {
        QrTokenResponse data = qrTokenService.startSession(slotId, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201,
                        "Attendance session started. Display the QR on screen.", data));
    }

    /**
     * GET /api/teacher/qr/{slotId}/current
     * Poll this endpoint every ~30 seconds.
     * Returns the currently ACTIVE token (auto-rotated by server).
     * If secondsUntilExpiry < 15, a new token will be issued soon — refresh again.
     *
     * Errors: 404 — no active session (teacher hasn't started one)
     */
    @GetMapping("/{slotId}/current")
    public ResponseEntity<ApiResponse<QrTokenResponse>> getCurrentToken(
            @PathVariable UUID slotId) {
        QrTokenResponse data = qrTokenService.getCurrentToken(slotId);
        return ResponseEntity.ok(ApiResponse.success("Current QR token retrieved", data));
    }

    /**
     * POST /api/teacher/qr/{slotId}/rotate
     * Force-rotate the QR token immediately (e.g. teacher suspects screenshot sharing).
     * Old token expires instantly, new one issued.
     */
    @PostMapping("/{slotId}/rotate")
    public ResponseEntity<ApiResponse<QrTokenResponse>> rotateToken(
            @PathVariable UUID slotId,
            @AuthenticationPrincipal UserDetails userDetails) {
        QrTokenResponse data = qrTokenService.rotateToken(slotId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("QR token rotated", data));
    }

    /**
     * POST /api/teacher/qr/{slotId}/end
     * Teacher ends the attendance session.
     * - Active QR token is revoked (no more scans accepted)
     * - All enrolled students who did NOT scan are marked ABSENT
     *
     * Pass the session date as query param: ?date=2026-08-08
     */
    @PostMapping("/{slotId}/end")
    public ResponseEntity<ApiResponse<Void>> endSession(
            @PathVariable UUID slotId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        // Revoke the active QR
        qrTokenService.endSession(slotId, userDetails.getUsername());

        // Mark all non-scanners as ABSENT
        LocalDate sessionDate = date != null ? date : LocalDate.now();
        attendanceService.markAbsentees(slotId, sessionDate);

        return ResponseEntity.ok(ApiResponse.success(
                "Session ended. Absent students have been recorded."));
    }
}
