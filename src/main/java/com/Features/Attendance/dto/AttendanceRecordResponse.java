package com.Features.Attendance.dto;

import com.Features.Attendance.model.AttendanceStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttendanceRecordResponse {

    private UUID             id;
    private UUID             studentId;
    private String           studentName;
    private String           studentEmail;

    private UUID             slotId;
    private String           subjectName;
    private String           subjectCode;
    private String           sectionName;
    private String           classroomName;
    private String           startTime;
    private String           endTime;

    private LocalDate        attendanceDate;
    private AttendanceStatus status;
    private LocalDateTime    markedAt;
}
