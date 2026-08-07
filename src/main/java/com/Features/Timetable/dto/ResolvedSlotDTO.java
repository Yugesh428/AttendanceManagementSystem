package com.Features.Timetable.dto;

import com.Features.Timetable.model.DayOfWeek;
import com.Features.Timetable.model.SlotStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * What the teacher's dashboard actually shows for a given day.
 *
 * The service resolves base slot + exception → this final view.
 * If status = CANCELLED, the teacher knows class is off.
 * If status = SUBSTITUTED, this slot appears on the substitute teacher's dashboard too.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResolvedSlotDTO {

    private UUID slotId;
    private UUID exceptionId;          // present only when an exception overrides this slot

    private LocalDate date;
    private DayOfWeek dayOfWeek;

    // ── Resolved values (base or overridden) ──────────────────────────────────
    private LocalTime startTime;
    private LocalTime endTime;
    private String classroomName;
    private String subjectName;
    private String subjectCode;
    private String teacherName;        // may be substitute on exception days

    // ── Status ────────────────────────────────────────────────────────────────
    private SlotStatus status;         // ACTIVE / CANCELLED / RESCHEDULED / SUBSTITUTED
    private String reason;             // exception reason, if any

    // ── Flags for easy UI rendering ───────────────────────────────────────────
    private boolean hasException;
    private boolean isCancelled;
    private boolean isSubstituted;
}
