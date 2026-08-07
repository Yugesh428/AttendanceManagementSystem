package com.Features.Admin.Teacher.controller;

import com.Features.Teacher.dto.TeacherDTO;
import com.Features.Teacher.service.TeacherService;
import com.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/teachers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminTeacherController {

    private final TeacherService teacherService;

    /**
     * POST /api/admin/teachers
     * Register a Faculty member as a Teacher.
     * Auto-creates the teacher login account.
     *
     * Request body:
     * {
     *   "facultyId": "<uuid>",
     *   "notes": "optional notes"
     * }
     *
     * Response includes generatedPassword — share it with the teacher.
     * It is shown ONLY ONCE and never stored in plain text.
     *
     * Errors:
     *   404 — Faculty not found
     *   409 — Faculty is already a Teacher
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TeacherDTO>> registerTeacher(
            @Valid @RequestBody TeacherDTO dto) {
        TeacherDTO data = teacherService.registerTeacher(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201,
                        "Teacher registered successfully. Share the generatedPassword with the teacher.", data));
    }

    /**
     * GET /api/admin/teachers
     * List all registered teachers with their faculty details.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TeacherDTO>>> getAllTeachers() {
        List<TeacherDTO> data = teacherService.getAllTeachers();
        return ResponseEntity.ok(ApiResponse.success("Teachers retrieved successfully", data));
    }

    /**
     * GET /api/admin/teachers/{id}
     * Get one teacher by teacher ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TeacherDTO>> getTeacherById(@PathVariable UUID id) {
        TeacherDTO data = teacherService.getTeacherById(id);
        return ResponseEntity.ok(ApiResponse.success("Teacher retrieved successfully", data));
    }

    /**
     * PUT /api/admin/teachers/{id}
     * Update teacher notes or active status.
     * Does NOT reset the password.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TeacherDTO>> updateTeacher(
            @PathVariable UUID id,
            @RequestBody TeacherDTO dto) {
        TeacherDTO data = teacherService.updateTeacher(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Teacher updated successfully", data));
    }

    /**
     * DELETE /api/admin/teachers/{id}
     * Remove a teacher (also removes their login account via cascade).
     * The underlying Faculty record is NOT deleted.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTeacher(@PathVariable UUID id) {
        teacherService.deleteTeacher(id);
        return ResponseEntity.ok(ApiResponse.success("Teacher deleted successfully"));
    }
}
