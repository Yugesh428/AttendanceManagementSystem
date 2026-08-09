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
    List<ResolvedSlotDTO> getMyTodaySchedule(String teacherEmail);
    List<ResolvedSlotDTO> getMyWeekSchedule(String teacherEmail, LocalDate weekStart);

    // ── Resolved dashboard (Student) ──────────────────────────────────────────
    /**
     * Returns today's resolved schedule for the student's section.
     * Looks up the student's active enrollment → section → slots for that section today.
     */
    List<ResolvedSlotDTO> getStudentTodaySchedule(String studentEmail);

    /**
     * Returns resolved week schedule for the student's section.
     */
    List<ResolvedSlotDTO> getStudentWeekSchedule(String studentEmail, LocalDate weekStart);
}
