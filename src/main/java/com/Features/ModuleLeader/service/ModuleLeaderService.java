package com.Features.ModuleLeader.service;

import com.Features.Attendance.dto.AttendanceReportResponse;
import com.Features.Attendance.dto.SlotHistoryResponse;
import com.Features.ModuleLeader.dto.*;
import com.Features.Timetable.dto.TimetableSlotDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ModuleLeaderService {

    // ── Admin operations ──────────────────────────────────────────────────────
    ModuleLeaderResponse create(CreateModuleLeaderRequest request);
    ModuleLeaderResponse getById(UUID id);
    List<ModuleLeaderResponse> getAll();
    ModuleLeaderResponse update(UUID id, CreateModuleLeaderRequest request);
    void delete(UUID id);

    // ── Auth ──────────────────────────────────────────────────────────────────
    ModuleLeaderLoginResponse login(ModuleLeaderLoginRequest request);

    // ── Module Leader dashboard ───────────────────────────────────────────────

    /** Own profile */
    ModuleLeaderResponse getMyProfile(String email);

    /** All timetable slots for their subject */
    List<TimetableSlotDTO> getMySlotsSubject(String email);

    /** Attendance report for one slot on one date */
    AttendanceReportResponse getSlotReport(String email, UUID slotId, LocalDate date);

    /** Full history for one slot (all past sessions) */
    SlotHistoryResponse getSlotHistory(String email, UUID slotId);

    /** All students + attendance % for their subject */
    List<StudentAttendanceSummaryDTO> getStudentSummaries(String email);

    /** All teachers who have slots for their subject */
    List<TeacherSummaryDTO> getTeachersForSubject(String email);
}
