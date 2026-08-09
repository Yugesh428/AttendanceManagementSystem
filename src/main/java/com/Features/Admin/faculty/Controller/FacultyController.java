package com.Features.Admin.faculty.Controller;

import com.Features.Admin.faculty.DTO.FacultyDTO;
import com.Features.Admin.faculty.Service.FacultyService;
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
@RequestMapping("/api/admin/faculties")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class FacultyController {

    private final FacultyService facultyService;

    @PostMapping
    public ResponseEntity<ApiResponse<FacultyDTO>> createFaculty(
            @Valid @RequestBody FacultyDTO dto) {
        FacultyDTO created = facultyService.createFaculty(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Faculty created successfully", created));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FacultyDTO>>> getAllFaculty() {
        return ResponseEntity.ok(
                ApiResponse.success("Faculty retrieved successfully", facultyService.getAllFaculty()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FacultyDTO>> getFacultyById(@PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.success("Faculty retrieved successfully", facultyService.getFacultyById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FacultyDTO>> updateFaculty(
            @PathVariable UUID id,
            @Valid @RequestBody FacultyDTO dto) {
        return ResponseEntity.ok(
                ApiResponse.success("Faculty updated successfully", facultyService.updateFaculty(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFaculty(@PathVariable UUID id) {
        facultyService.deleteFaculty(id);
        return ResponseEntity.ok(ApiResponse.success("Faculty deleted successfully"));
    }
}
