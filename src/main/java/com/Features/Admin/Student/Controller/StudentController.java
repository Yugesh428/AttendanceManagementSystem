package com.Features.Admin.Student.Controller;

import com.Features.Admin.Student.DTO.StudentDTO;
import com.Features.Admin.Student.service.StudentService;
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
@RequestMapping("/api/admin/students")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class StudentController {

    private static final String XLSX_MIME =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final StudentService studentService;

    // ── CRUD ──────────────────────────────────────────────────────────────────

    /**
     * POST /api/admin/students
     * Creates a student and auto-generates their login account.
     * Response includes generatedPassword — share it with the student.
     * It is shown ONLY once and never stored in plain text.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<StudentDTO>> createStudent(
            @Valid @RequestBody StudentDTO dto) {
        StudentDTO data = studentService.createStudent(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201,
                        "Student created successfully. Share the generatedPassword with the student.", data));
    }

    /** GET /api/admin/students */
    @GetMapping
    public ResponseEntity<ApiResponse<List<StudentDTO>>> getAllStudents() {
        List<StudentDTO> data = studentService.findAllStudents();
        return ResponseEntity.ok(
                ApiResponse.success("Students retrieved successfully", data));
    }

    /** GET /api/admin/students/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentDTO>> getStudentById(@PathVariable UUID id) {
        StudentDTO data = studentService.findStudentById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Student retrieved successfully", data));
    }

    /** PUT /api/admin/students/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentDTO>> updateStudent(
            @PathVariable UUID id,
            @Valid @RequestBody StudentDTO dto) {
        StudentDTO data = studentService.updateStudent(id, dto);
        return ResponseEntity.ok(
                ApiResponse.success("Student updated successfully", data));
    }

    /** DELETE /api/admin/students/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable UUID id) {
        studentService.deleteStudent(id);
        return ResponseEntity.ok(
                ApiResponse.success("Student deleted successfully"));
    }

    // ── Excel ─────────────────────────────────────────────────────────────────

    /**
     * GET /api/admin/students/excel/export
     * Downloads all students as .xlsx.
     */
    @GetMapping("/excel/export")
    public ResponseEntity<InputStreamResource> exportToExcel() {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(XLSX_MIME))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"students.xlsx\"")
                .body(new InputStreamResource(studentService.exportToExcel()));
    }

    /**
     * GET /api/admin/students/excel/template
     * Downloads blank import template.
     * Columns: First Name * | Last Name | Email * | Phone |
     *          Father Name | Father Phone | Mother Name | Mother Phone |
     *          Guardian Name | Guardian Phone
     */
    @GetMapping("/excel/template")
    public ResponseEntity<InputStreamResource> downloadTemplate() {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(XLSX_MIME))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"students_template.xlsx\"")
                .body(new InputStreamResource(studentService.downloadTemplate()));
    }

    /**
     * POST /api/admin/students/excel/import
     * Bulk-create students from an .xlsx file.
     * A login account is auto-created for EACH student.
     * The response includes generatedPassword for every student in the list —
     * share these credentials with the students before the next request
     * (generatedPassword is not stored and will not appear again).
     *
     * Returns 422 on bad data (duplicate email, missing required field, etc.)
     */
    @PostMapping(value = "/excel/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<StudentDTO>>> importFromExcel(
            @RequestParam("file") MultipartFile file) {
        List<StudentDTO> data = studentService.importFromExcel(file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201,
                        data.size() + " student(s) imported. Share the generatedPassword with each student.", data));
    }
}
