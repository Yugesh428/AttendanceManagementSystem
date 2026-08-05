package com.Features.Admin.Classroom.Controller;

import com.Features.Admin.Classroom.ClassroomService.ClassroomService;
import com.Features.Admin.Classroom.DTO.ClassroomDTO;
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
@RequestMapping("/api/admin/classrooms")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ClassroomController {

    private static final String XLSX_MIME =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final ClassroomService classroomService;

    // ── CRUD ──────────────────────────────────────────────────────────────────

    /**
     * POST /api/admin/classrooms
     * classType: LECTURE | TUTORIAL | PRACTICAL
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ClassroomDTO>> createClassroom(
            @Valid @RequestBody ClassroomDTO dto) {
        ClassroomDTO data = classroomService.createClassroom(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Classroom created successfully", data));
    }

    /** GET /api/admin/classrooms */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ClassroomDTO>>> getAllClassrooms() {
        List<ClassroomDTO> data = classroomService.findAllClassrooms();
        return ResponseEntity.ok(
                ApiResponse.success("Classrooms retrieved successfully", data));
    }

    /** GET /api/admin/classrooms/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClassroomDTO>> getClassroomById(@PathVariable UUID id) {
        ClassroomDTO data = classroomService.findClassroomById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Classroom retrieved successfully", data));
    }

    /** PUT /api/admin/classrooms/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClassroomDTO>> updateClassroom(
            @PathVariable UUID id,
            @Valid @RequestBody ClassroomDTO dto) {
        ClassroomDTO data = classroomService.updateClassroom(id, dto);
        return ResponseEntity.ok(
                ApiResponse.success("Classroom updated successfully", data));
    }

    /** DELETE /api/admin/classrooms/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteClassroom(@PathVariable UUID id) {
        classroomService.deleteClassroom(id);
        return ResponseEntity.ok(
                ApiResponse.success("Classroom deleted successfully"));
    }

    // ── Excel ─────────────────────────────────────────────────────────────────

    /**
     * GET /api/admin/classrooms/excel/export
     * Downloads all classrooms as .xlsx (binary).
     * Columns: ID | Name | Class Type | Building ID | Building Name | Created At
     */
    @GetMapping("/excel/export")
    public ResponseEntity<InputStreamResource> exportToExcel() {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(XLSX_MIME))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"classrooms.xlsx\"")
                .body(new InputStreamResource(classroomService.exportToExcel()));
    }

    /**
     * GET /api/admin/classrooms/excel/template
     * Downloads blank import template.
     * Import columns: Name * | Class Type * (LECTURE/TUTORIAL/PRACTICAL) | Building ID *
     */
    @GetMapping("/excel/template")
    public ResponseEntity<InputStreamResource> downloadTemplate() {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(XLSX_MIME))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"classrooms_template.xlsx\"")
                .body(new InputStreamResource(classroomService.downloadTemplate()));
    }

    /**
     * POST /api/admin/classrooms/excel/import
     * Bulk-create classrooms from an .xlsx file.
     * Returns 201 + list of created classrooms wrapped in ApiResponse.
     * Returns 422 on bad data — message includes exact row number and field.
     */
    @PostMapping(value = "/excel/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<ClassroomDTO>>> importFromExcel(
            @RequestParam("file") MultipartFile file) {
        List<ClassroomDTO> data = classroomService.importFromExcel(file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201,
                        data.size() + " classroom(s) imported successfully", data));
    }
}
