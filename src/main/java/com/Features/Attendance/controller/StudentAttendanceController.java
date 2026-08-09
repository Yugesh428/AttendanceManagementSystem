package com.Features.Attendance.controller;

import com.Features.Attendance.dto.AttendanceRecordResponse;
import com.Features.Attendance.dto.AttendanceReportResponse;
import com.Features.Attendance.dto.AttendanceScanRequest;
import com.Features.Attendance.service.AttendanceService;
import com.common.ApiResponse;
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
@RequestMapping("/api/student/attendance")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentAttendanceController {

    private final AttendanceService attendanceService;

    /**
     * POST /api/student/attendance/scan
     * Student scans QR and marks attendance.
     *
     * Runs 6 validations:
     *   1. QR valid + not expired
     *   2. deviceId matches registered phone
     *   3. beaconId matches classroom
     *   4. student's section matches slot's section
     *   5. student enrolled in the subject
     *   6. not already marked today
     *
     * Body:
     * {
     *   "qrToken":  "<uuid-from-qr-url>",
     *   "deviceId": "<localStorage-fingerprint>",
     *   "beaconId": "<beacon-uuid-from-ble-scan>"
     * }
     */
    @PostMapping("/scan")
    public ResponseEntity<ApiResponse<AttendanceRecordResponse>> scan(
            @Valid @RequestBody AttendanceScanRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        AttendanceRecordResponse data =
                attendanceService.scan(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Attendance marked successfully", data));
    }

    /**
     * GET /api/student/attendance/history?from=2026-08-01&to=2026-08-31
     * Student views their own attendance records for a date range.
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<AttendanceRecordResponse>>> getHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<AttendanceRecordResponse> data =
                attendanceService.getMyAttendance(userDetails.getUsername(), from, to);
        return ResponseEntity.ok(ApiResponse.success("Attendance history retrieved", data));
    }

    /**
     * GET /api/student/attendance/summary
     * Student sees their attendance % per subject for the current active semester.
     *
     * Response example:
     * [
     *   { "subjectName": "Math", "totalClasses": 20, "presentCount": 17, "percentage": 85.0 },
     *   { "subjectName": "CS",   "totalClasses": 18, "presentCount": 14, "percentage": 77.8 }
     * ]
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<List<AttendanceReportResponse.SubjectAttendanceSummary>>> getSummary(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<AttendanceReportResponse.SubjectAttendanceSummary> data =
                attendanceService.getMySubjectSummary(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Attendance summary retrieved", data));
    }

    /**
     * GET /api/student/attendance/today
     * Quick view of today's attendance status across all scheduled classes.
     *
     * Shows which classes the student has already marked attendance for today
     * and which they haven't yet. Frontend uses this for a dashboard status card.
     *
     * Equivalent to /history?from=today&to=today but no params needed.
     */
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<AttendanceRecordResponse>>> getToday(
            @AuthenticationPrincipal UserDetails userDetails) {
        LocalDate today = LocalDate.now();
        List<AttendanceRecordResponse> data =
                attendanceService.getMyAttendance(userDetails.getUsername(), today, today);
        return ResponseEntity.ok(ApiResponse.success("Today's attendance retrieved", data));
    }
}
