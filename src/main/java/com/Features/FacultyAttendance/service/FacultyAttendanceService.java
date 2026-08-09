package com.Features.FacultyAttendance.service;

import com.Features.FacultyAttendance.dto.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface FacultyAttendanceService {

    // ── Admin: QR management ─────────────────────────────────────────────────
    /** Generate daily QR for today (or a specific date) */
    FacultyDailyQrResponse generateDailyQr(LocalDate date, String adminEmail);

    /** Get today's active QR — faculty view this to scan */
    FacultyDailyQrResponse getTodayQr();

    /** Admin revokes today's QR */
    void revokeQr(UUID qrId);

    // ── Faculty: scan QR ─────────────────────────────────────────────────────
    /**
     * Faculty scans the daily QR.
     * Validates: QR exists + not expired + faculty on college WiFi + no duplicate.
     */
    FacultyAttendanceRecordResponse scan(String qrToken, String facultyEmail, String clientIp);

    // ── Admin: reports ────────────────────────────────────────────────────────
    /** Full attendance report for a specific date */
    FacultyAttendanceReportResponse getDailyReport(LocalDate date);

    /** Report for a specific department on a date */
    FacultyAttendanceReportResponse getDepartmentReport(UUID departmentId, LocalDate date);

    /** Attendance history for one faculty by ID */
    List<FacultyAttendanceRecordResponse> getFacultyHistory(UUID facultyId,
                                                             LocalDate from, LocalDate to);

    /** Faculty views their own history by email */
    List<FacultyAttendanceRecordResponse> getMyHistory(String facultyEmail,
                                                        LocalDate from, LocalDate to);

    /** Admin manually marks a faculty's attendance (e.g. on leave) */
    FacultyAttendanceRecordResponse override(UUID facultyId, LocalDate date,
                                              String status, String reason);

    /** Scheduled: auto-expire yesterday's QR at midnight */
    void expireOldQrCodes();
}
