package com.Features.Attendance.service;

import com.Features.Attendance.dto.AttendanceRecordResponse;
import com.Features.Attendance.dto.AttendanceReportResponse;
import com.Features.Attendance.dto.AttendanceScanRequest;
import com.Features.Attendance.dto.SlotHistoryResponse;
import com.Features.Attendance.dto.SlotStudentResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AttendanceService {

    /** Student scans QR — validates all 6 conditions and marks PRESENT */
    AttendanceRecordResponse scan(AttendanceScanRequest request, String studentEmail);

    /** Session ends — mark all enrolled students who didn't scan as ABSENT */
    void markAbsentees(UUID slotId, LocalDate date);

    /** Admin/teacher: full report for a slot on a date */
    AttendanceReportResponse getReport(UUID slotId, LocalDate date);

    /** Admin: all records for a section on a date */
    List<AttendanceRecordResponse> getBySection(UUID sectionId, LocalDate date);

    /** Student: own attendance records in a date range */
    List<AttendanceRecordResponse> getMyAttendance(String studentEmail,
                                                    LocalDate from, LocalDate to);

    /** Student: attendance % per subject */
    List<AttendanceReportResponse.SubjectAttendanceSummary> getMySubjectSummary(
            String studentEmail);

    /** Admin: manually override a student's attendance status */
    AttendanceRecordResponse override(UUID studentId, UUID slotId,
                                      LocalDate date, String status, String reason);

    /**
     * Teacher: list all students enrolled in a slot's section + subject.
     * Teacher sees who is in their class before starting a session.
     */
    List<SlotStudentResponse> getStudentsForSlot(UUID slotId);

    /**
     * Teacher: full attendance history for a slot across all past session dates.
     * Each date shows a session summary with present/absent breakdown.
     */
    SlotHistoryResponse getSlotHistory(UUID slotId);
}
