package com.Features.Attendance.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Historical attendance for one slot across all past sessions.
 * Teacher sees every date the slot has run and each student's status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SlotHistoryResponse {

    private UUID   slotId;
    private String subjectName;
    private String subjectCode;
    private String sectionName;
    private String classroomName;
    private String dayOfWeek;
    private String startTime;
    private String endTime;

    /** All dates this slot has had an attendance session */
    private List<LocalDate> sessionDates;

    /**
     * Records grouped — each entry is one session date with its student records.
     * Sorted newest date first.
     */
    private List<SessionSummary> sessions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionSummary {
        private LocalDate date;
        private int       totalEnrolled;
        private int       presentCount;
        private int       absentCount;
        private double    attendancePercentage;
        private List<AttendanceRecordResponse> records;
    }
}
