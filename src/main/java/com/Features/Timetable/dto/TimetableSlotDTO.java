package com.Features.Timetable.dto;

import com.Features.Timetable.model.DayOfWeek;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TimetableSlotDTO {

    private UUID id;

    @NotNull(message = "Teacher ID is required")
    private UUID teacherId;
    private String teacherName;       // response only

    @NotNull(message = "Subject ID is required")
    private UUID subjectId;
    private String subjectName;       // response only
    private String subjectCode;       // response only

    @NotNull(message = "Classroom ID is required")
    private UUID classroomId;
    private String classroomName;     // response only

    @NotNull(message = "Section ID is required")
    private UUID sectionId;
    private String sectionName;       // response only

    @NotNull(message = "Day of week is required")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotNull(message = "Effective from date is required")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;    // null = open-ended

    private String notes;
    private LocalDateTime createdAt;
}
