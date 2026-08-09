package com.Features.FacultyAttendance.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FacultyAttendanceReportResponse {

    private LocalDate                          date;
    private int                                totalFaculty;
    private int                                presentCount;
    private int                                absentCount;
    private double                             attendancePercentage;
    private List<FacultyAttendanceRecordResponse> records;
}
