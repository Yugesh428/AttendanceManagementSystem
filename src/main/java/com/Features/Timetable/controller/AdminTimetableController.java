package com.Features.Timetable.controller;

import com.Features.Timetable.dto.TimetableExceptionDTO;
import com.Features.Timetable.dto.TimetableSlotDTO;
import com.Features.Timetable.service.TimetableService;
import com.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/timetable")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminTimetableController {

    private static final String XLSX_MIME =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final TimetableService timetableService;

    // ── Slots CRUD ─────────────────────────────────────────────────────────────

    /**
     * POST /api/admin/timetable/slots
     * Create one recurring weekly slot.
     * Returns 409 if teacher or classroom clash is detected.
     */
    @PostMapping("/slots")
    public ResponseEntity<ApiResponse<TimetableSlotDTO>> createSlot(
            @Valid @RequestBody TimetableSlotDTO dto) {
        TimetableSlotDTO data = timetableService.createSlot(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Timetable slot created successfully", data));
    }

    /** GET /api/admin/timetable/slots — all slots */
    @GetMapping("/slots")
    public ResponseEntity<ApiResponse<List<TimetableSlotDTO>>> getAllSlots() {
        return ResponseEntity.ok(
                ApiResponse.success("Slots retrieved successfully", timetableService.getAllSlots()));
    }

    /** GET /api/admin/timetable/slots/{id} */
    @GetMapping("/slots/{id}")
    public ResponseEntity<ApiResponse<TimetableSlotDTO>> getSlotById(@PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.success("Slot retrieved successfully", timetableService.getSlotById(id)));
    }

    /** GET /api/admin/timetable/slots/teacher/{teacherId} — filter by teacher */
    @GetMapping("/slots/teacher/{teacherId}")
    public ResponseEntity<ApiResponse<List<TimetableSlotDTO>>> getSlotsByTeacher(
            @PathVariable UUID teacherId) {
        return ResponseEntity.ok(
                ApiResponse.success("Slots retrieved successfully",
                        timetableService.getSlotsByTeacher(teacherId)));
    }

    /** PUT /api/admin/timetable/slots/{id} */
    @PutMapping("/slots/{id}")
    public ResponseEntity<ApiResponse<TimetableSlotDTO>> updateSlot(
            @PathVariable UUID id,
            @Valid @RequestBody TimetableSlotDTO dto) {
        return ResponseEntity.ok(
                ApiResponse.success("Slot updated successfully", timetableService.updateSlot(id, dto)));
    }

    /** DELETE /api/admin/timetable/slots/{id} — also removes all exceptions for this slot */
    @DeleteMapping("/slots/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSlot(@PathVariable UUID id) {
        timetableService.deleteSlot(id);
        return ResponseEntity.ok(ApiResponse.success("Slot deleted successfully"));
    }

    // ── Excel ──────────────────────────────────────────────────────────────────

    /**
     * GET /api/admin/timetable/slots/excel/export
     * Downloads all timetable slots as .xlsx
     */
    @GetMapping("/slots/excel/export")
    public ResponseEntity<InputStreamResource> exportToExcel() {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(XLSX_MIME))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"timetable.xlsx\"")
                .body(new InputStreamResource(timetableService.exportSlotsToExcel()));
    }

    /**
     * GET /api/admin/timetable/slots/excel/template
     * Downloads import template.
     * Columns: Teacher ID | Subject ID | Classroom ID | Day | Start | End | From | To | Notes
     */
    @GetMapping("/slots/excel/template")
    public ResponseEntity<InputStreamResource> downloadTemplate() {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(XLSX_MIME))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"timetable_template.xlsx\"")
                .body(new InputStreamResource(timetableService.downloadSlotTemplate()));
    }

    /**
     * POST /api/admin/timetable/slots/excel/import
     * Bulk-create slots from .xlsx. Clash checks run per row.
     * Returns 422 on bad data, 409 on clash.
     */
    @PostMapping(value = "/slots/excel/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<TimetableSlotDTO>>> importFromExcel(
            @RequestParam("file") MultipartFile file) {
        List<TimetableSlotDTO> data = timetableService.importSlotsFromExcel(file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201,
                        data.size() + " slot(s) imported successfully", data));
    }

    // ── Exceptions (one-off overrides) ────────────────────────────────────────

    /**
     * POST /api/admin/timetable/exceptions
     * Override a slot on a specific date.
     *
     * status = CANCELLED     → class is off
     * status = RESCHEDULED   → different time/room on this date
     * status = SUBSTITUTED   → different teacher on this date
     */
    @PostMapping("/exceptions")
    public ResponseEntity<ApiResponse<TimetableExceptionDTO>> createException(
            @Valid @RequestBody TimetableExceptionDTO dto) {
        TimetableExceptionDTO data = timetableService.createException(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Exception created successfully", data));
    }

    /**
     * GET /api/admin/timetable/exceptions?date=2026-08-10
     * All exceptions for a given date (admin view of what changed today).
     */
    @GetMapping("/exceptions")
    public ResponseEntity<ApiResponse<List<TimetableExceptionDTO>>> getExceptionsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(
                ApiResponse.success("Exceptions retrieved successfully",
                        timetableService.getExceptionsByDate(date)));
    }

    /**
     * GET /api/admin/timetable/exceptions/slot/{slotId}
     * All exceptions ever created for one base slot.
     */
    @GetMapping("/exceptions/slot/{slotId}")
    public ResponseEntity<ApiResponse<List<TimetableExceptionDTO>>> getExceptionsBySlot(
            @PathVariable UUID slotId) {
        return ResponseEntity.ok(
                ApiResponse.success("Exceptions retrieved successfully",
                        timetableService.getExceptionsBySlot(slotId)));
    }

    /** PUT /api/admin/timetable/exceptions/{id} — edit an existing exception */
    @PutMapping("/exceptions/{id}")
    public ResponseEntity<ApiResponse<TimetableExceptionDTO>> updateException(
            @PathVariable UUID id,
            @Valid @RequestBody TimetableExceptionDTO dto) {
        return ResponseEntity.ok(
                ApiResponse.success("Exception updated successfully",
                        timetableService.updateException(id, dto)));
    }

    /** DELETE /api/admin/timetable/exceptions/{id} — restore the base slot on that date */
    @DeleteMapping("/exceptions/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteException(@PathVariable UUID id) {
        timetableService.deleteException(id);
        return ResponseEntity.ok(ApiResponse.success("Exception deleted successfully"));
    }
}
