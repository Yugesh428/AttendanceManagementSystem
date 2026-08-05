package com.Features.Admin.Department.Controller;

import com.Features.Admin.Department.DTO.DepartmentDTO;
import com.Features.Admin.Department.service.DepartmentService;
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
@RequestMapping("/api/admin/departments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DepartmentController {

    private static final String XLSX_MIME =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final DepartmentService service;

    // ── CRUD ──────────────────────────────────────────────────────────────────

    /** POST /api/admin/departments */
    @PostMapping
    public ResponseEntity<ApiResponse<DepartmentDTO>> createDepartment(
            @Valid @RequestBody DepartmentDTO dto) {
        DepartmentDTO data = service.createDepartmentDTO(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Department created successfully", data));
    }

    /** GET /api/admin/departments */
    @GetMapping
    public ResponseEntity<ApiResponse<List<DepartmentDTO>>> getAllDepartments() {
        List<DepartmentDTO> data = service.getAllDepartmentDTOs();
        return ResponseEntity.ok(
                ApiResponse.success("Departments retrieved successfully", data));
    }

    /** GET /api/admin/departments/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentDTO>> getDepartmentById(@PathVariable UUID id) {
        DepartmentDTO data = service.getDepartmentDTOById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Department retrieved successfully", data));
    }

    /** PUT /api/admin/departments/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentDTO>> updateDepartment(
            @PathVariable UUID id,
            @Valid @RequestBody DepartmentDTO dto) {
        DepartmentDTO data = service.updateDepartmentDTO(id, dto);
        return ResponseEntity.ok(
                ApiResponse.success("Department updated successfully", data));
    }

    /** DELETE /api/admin/departments/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable UUID id) {
        service.deleteDepartmentDTOById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Department deleted successfully"));
    }

    // ── Excel ─────────────────────────────────────────────────────────────────

    /**
     * GET /api/admin/departments/excel/export
     * Downloads all departments as .xlsx.
     * Columns: ID | Name | Code | Description | Created At
     */
    @GetMapping("/excel/export")
    public ResponseEntity<InputStreamResource> exportToExcel() {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(XLSX_MIME))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"departments.xlsx\"")
                .body(new InputStreamResource(service.exportToExcel()));
    }

    /**
     * GET /api/admin/departments/excel/template
     * Downloads blank import template.
     * Import columns: Name * | Code * | Description
     */
    @GetMapping("/excel/template")
    public ResponseEntity<InputStreamResource> downloadTemplate() {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(XLSX_MIME))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"departments_template.xlsx\"")
                .body(new InputStreamResource(service.downloadTemplate()));
    }

    /**
     * POST /api/admin/departments/excel/import
     * Bulk-create departments from an .xlsx file.
     * Returns 201 + created list on success.
     * Returns 422 on bad/duplicate data with row-level error message.
     */
    @PostMapping(value = "/excel/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<DepartmentDTO>>> importFromExcel(
            @RequestParam("file") MultipartFile file) {
        List<DepartmentDTO> data = service.importFromExcel(file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201,
                        data.size() + " department(s) imported successfully", data));
    }
}
