package com.Features.ModuleLeader.dto;

import lombok.*;

import java.util.UUID;

/**
 * Per-student attendance summary for a subject.
 * Used by module leader to see how each student is performing in their subject.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentAttendanceSummaryDTO {

    private UUID   studentId;
    private String studentName;
    private String studentEmail;
    private String sectionName;

    private UUID   subjectId;
    private String subjectName;
    private String subjectCode;

    private long   totalClasses;
    private long   presentCount;
    private long   absentCount;
    private double attendancePercentage;
}
