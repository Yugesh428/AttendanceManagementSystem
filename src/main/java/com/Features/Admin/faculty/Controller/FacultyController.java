package com.Features.Admin.faculty.Controller;


import com.Features.Admin.faculty.DTO.FacultyDTO;
import com.Features.Admin.faculty.Service.FacultyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/faculty")
@RequiredArgsConstructor
public class FacultyController {

    private final FacultyService facultyService;

    // ── CREATE ────────────────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<FacultyDTO> createFaculty(
            @Valid @RequestBody FacultyDTO dto) {

        FacultyDTO created = facultyService.createFaculty(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // ── GET ALL ───────────────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<FacultyDTO>> getAllFaculty() {
        return ResponseEntity.ok(facultyService.getAllFaculty());
    }

    // ── GET BY ID ─────────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<FacultyDTO> getFacultyById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(facultyService.getFacultyById(id));
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<FacultyDTO> updateFaculty(
            @PathVariable UUID id,
            @Valid @RequestBody FacultyDTO dto) {

        return ResponseEntity.ok(facultyService.updateFaculty(id, dto));
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFaculty(
            @PathVariable UUID id) {

        facultyService.deleteFaculty(id);
        return ResponseEntity.ok("Faculty deleted successfully");
    }
}
