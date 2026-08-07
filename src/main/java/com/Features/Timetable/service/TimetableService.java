package com.Features.Timetable.service;

import com.Features.Timetable.dto.ResolvedSlotDTO;
import com.Features.Timetable.dto.TimetableExceptionDTO;
import com.Features.Timetable.dto.TimetableSlotDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TimetableService {

    // ── Slots (Admin) ──────────────────────────────────────────────────────────
    TimetableSlotDTO createSlot(TimetableSlotDTO dto);
    TimetableSlotDTO getSlotById(UUID id);
    List<TimetableSlotDTO> getAllSlots();
    List<TimetableSlotDTO> getSlotsByTeacher(UUID teacherId);
    TimetableSlotDTO updateSlot(UUID id, TimetableSlotDTO dto);
    void deleteSlot(UUID id);

    // ── Excel (Admin) ──────────────────────────────────────────────────────────
    ByteArrayInputStream exportSlotsToExcel();
    ByteArrayInputStream downloadSlotTemplate();
    List<TimetableSlotDTO> importSlotsFromExcel(MultipartFile file);

    // ── Exceptions / overrides (Admin) ────────────────────────────────────────
    TimetableExceptionDTO createException(TimetableExceptionDTO dto);
    TimetableExceptionDTO updateException(UUID id, TimetableExceptionDTO dto);
    void deleteException(UUID id);
    List<TimetableExceptionDTO> getExceptionsByDate(LocalDate date);
    List<TimetableExceptionDTO> getExceptionsBySlot(UUID slotId);

    // ── Resolved dashboard (Teacher) ──────────────────────────────────────────
    /**
     * Returns today's resolved schedule for the teacher.
     * Base slots + exceptions merged — what the teacher actually sees.
     */
    List<ResolvedSlotDTO> getMyTodaySchedule(String teacherEmail);

    /**
     * Returns resolved schedule for teacher for a full week containing the given date.
     */
    List<ResolvedSlotDTO> getMyWeekSchedule(String teacherEmail, LocalDate weekStart);
}
