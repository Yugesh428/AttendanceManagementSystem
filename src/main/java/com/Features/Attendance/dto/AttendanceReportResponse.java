package com.Features.Attendance.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Admin/teacher report — attendance for a slot on a given date.
 * Shows each enrolled student with their PRESENT/ABSENT status
 * plus overall percentage for that session.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttendanceReportResponse {

    private UUID   slotId;
    private String subjectName;
    private String subjectCode;
    private String sectionName;
    private String classroomName;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private LocalDate date;

    private int    totalEnrolled;
    private int    presentCount;
    private int    absentCount;
    private double attendancePercentage;

    private List<AttendanceRecordResponse> records;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectAttendanceSummary {
        private UUID   subjectId;
        private String subjectName;
        private String subjectCode;
        private long   totalClasses;
        private long   presentCount;
        private double percentage;
    }
}
