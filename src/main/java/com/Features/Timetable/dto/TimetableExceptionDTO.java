package com.Features.Timetable.dto;

import com.Features.Timetable.model.SlotStatus;
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
public class TimetableExceptionDTO {

    private UUID id;

    @NotNull(message = "Slot ID is required")
    private UUID slotId;

    @NotNull(message = "Exception date is required")
    private LocalDate exceptionDate;

    @NotNull(message = "Status is required (CANCELLED / RESCHEDULED / SUBSTITUTED)")
    private SlotStatus status;

    // ── Override fields — all optional ────────────────────────────────────────
    private LocalTime overrideStartTime;
    private LocalTime overrideEndTime;

    private UUID overrideClassroomId;
    private String overrideClassroomName;     // response only

    private UUID substituteTeacherId;
    private String substituteTeacherName;     // response only

    private String reason;

    // ── Original slot info (response only — for display) ──────────────────────
    private String teacherName;
    private String subjectName;
    private String subjectCode;
    private String originalClassroomName;
    private LocalTime originalStartTime;
    private LocalTime originalEndTime;

    private LocalDateTime createdAt;
}
