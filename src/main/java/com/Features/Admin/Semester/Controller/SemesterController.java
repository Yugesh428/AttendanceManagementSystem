package com.Features.Admin.Semester.Controller;

import com.Features.Admin.Semester.DTO.SemesterDTO;
import com.Features.Admin.Semester.Service.SemesterService;
import com.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/semesters")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SemesterController {

    private static final String XLSX_MIME =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final SemesterService semesterService;

    // ── CRUD ─────────────────────────────────────────────────────────────

    /** POST /api/admin/semesters */
    @PostMapping
    public ResponseEntity<ApiResponse<SemesterDTO>> createSemester(
            @Valid @RequestBody SemesterDTO dto) {

        SemesterDTO data = semesterService.createSemester(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Semester created successfully", data));
    }

    /** GET /api/admin/semesters */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SemesterDTO>>> getAllSemesters() {

        List<SemesterDTO> data = semesterService.getAllSemesters();

        return ResponseEntity.ok(
                ApiResponse.success("Semesters retrieved successfully", data));
    }

    /** GET /api/admin/semesters/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SemesterDTO>> getSemesterById(
            @PathVariable UUID id) {

        SemesterDTO data = semesterService.getSemesterById(id);

        return ResponseEntity.ok(
                ApiResponse.success("Semester retrieved successfully", data));
    }

    /** PUT /api/admin/semesters/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SemesterDTO>> updateSemester(
            @PathVariable UUID id,
            @Valid @RequestBody SemesterDTO dto) {

        SemesterDTO data = semesterService.updateSemester(id, dto);

        return ResponseEntity.ok(
                ApiResponse.success("Semester updated successfully", data));
    }

    /** DELETE /api/admin/semesters/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSemester(
            @PathVariable UUID id) {

        semesterService.deleteSemester(id);

        return ResponseEntity.ok(
                ApiResponse.success("Semester deleted successfully"));
    }

    // ── Excel ────────────────────────────────────────────────────────────

    /** GET /api/admin/semesters/excel/export */
    @GetMapping("/excel/export")
    public ResponseEntity<InputStreamResource> exportToExcel() {

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(XLSX_MIME))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"semesters.xlsx\"")
                .body(new InputStreamResource(semesterService.exportToExcel()));
    }

    /** GET /api/admin/semesters/excel/template */
    @GetMapping("/excel/template")
    public ResponseEntity<InputStreamResource> downloadTemplate() {

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(XLSX_MIME))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"semesters_template.xlsx\"")
                .body(new InputStreamResource(semesterService.downloadTemplate()));
    }

    /** POST /api/admin/semesters/excel/import */
    @PostMapping(value = "/excel/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<SemesterDTO>>> importFromExcel(
            @RequestParam("file") MultipartFile file) {

        List<SemesterDTO> data = semesterService.importFromExcel(file);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        201,
                        data.size() + " semester(s) imported successfully",
                        data));
    }
}