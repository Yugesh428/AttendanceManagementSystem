package com.Features.FacultyAttendance.dto;

import com.Features.FacultyAttendance.model.FacultyAttendanceStatus;
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
public class FacultyAttendanceRecordResponse {

    private UUID                    id;
    private UUID                    facultyId;
    private String                  facultyName;
    private String                  facultyEmail;
    private String                  designation;
    private String                  departmentName;
    private LocalDate               attendanceDate;
    private FacultyAttendanceStatus status;
    private String                  scannedFromIp;
    private LocalDateTime           markedAt;
}
