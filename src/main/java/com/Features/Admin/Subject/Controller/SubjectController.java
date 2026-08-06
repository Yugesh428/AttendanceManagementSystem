package com.Features.Admin.Subject.Controller;

import com.Features.Admin.Subject.DTO.SubjectResponseDTO;
import com.Features.Admin.Subject.Service.SubjectService;
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
@RequestMapping("/api/admin/subjects")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SubjectController {

    private final SubjectService subjectService;

    /** POST /api/admin/subjects */
    @PostMapping
    public ResponseEntity<ApiResponse<SubjectResponseDTO>> createSubject(
            @Valid @RequestBody SubjectResponseDTO dto) {
        SubjectResponseDTO data = subjectService.createSubject(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Subject created successfully", data));
    }

    /** GET /api/admin/subjects */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SubjectResponseDTO>>> getAllSubjects() {
        List<SubjectResponseDTO> data = subjectService.getAllSubjects();
        return ResponseEntity.ok(
                ApiResponse.success("Subjects retrieved successfully", data));
    }

    /** GET /api/admin/subjects/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubjectResponseDTO>> getSubjectById(@PathVariable UUID id) {
        SubjectResponseDTO data = subjectService.getSubjectById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Subject retrieved successfully", data));
    }

    /** PUT /api/admin/subjects/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SubjectResponseDTO>> updateSubject(
            @PathVariable UUID id,
            @Valid @RequestBody SubjectResponseDTO dto) {
        SubjectResponseDTO data = subjectService.updateSubject(id, dto);
        return ResponseEntity.ok(
                ApiResponse.success("Subject updated successfully", data));
    }

    /** DELETE /api/admin/subjects/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSubject(@PathVariable UUID id) {
        subjectService.deleteSubject(id);
        return ResponseEntity.ok(
                ApiResponse.success("Subject deleted successfully"));
    }
}
