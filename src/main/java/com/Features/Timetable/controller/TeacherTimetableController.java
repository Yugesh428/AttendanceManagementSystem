package com.Features.Timetable.controller;

import com.Features.Timetable.dto.ResolvedSlotDTO;
import com.Features.Timetable.service.TimetableService;
import com.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/teacher/timetable")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherTimetableController {

    private final TimetableService timetableService;

    /**
     * GET /api/teacher/timetable/today
     * Teacher's resolved schedule for today.
     *
     * - Shows ACTIVE slots (normal class)
     * - Shows CANCELLED slots (so teacher knows class is off)
     * - Shows RESCHEDULED slots with new time/room
     * - Shows SUBSTITUTED slots (if this teacher is the substitute today)
     */
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<ResolvedSlotDTO>>> getToday(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ResolvedSlotDTO> data = timetableService.getMyTodaySchedule(userDetails.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("Today's schedule retrieved successfully", data));
    }

    /**
     * GET /api/teacher/timetable/week?weekStart=2026-08-10
     * Teacher's full resolved week schedule (Mon–Sat of that week).
     * weekStart must be a Monday.
     *
     * The teacher sees exactly what will happen on each day —
     * cancellations, room changes, and substitutions are all reflected.
     */
    @GetMapping("/week")
    public ResponseEntity<ApiResponse<List<ResolvedSlotDTO>>> getWeek(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        List<ResolvedSlotDTO> data = timetableService.getMyWeekSchedule(
                userDetails.getUsername(), weekStart);
        return ResponseEntity.ok(
                ApiResponse.success("Week schedule retrieved successfully", data));
    }
}
