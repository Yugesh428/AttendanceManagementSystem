package com.Features.Admin.Section.Controller;

import com.Features.Admin.Section.DTO.SectionDTO;
import com.Features.Admin.Section.Service.SectionService;
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
@RequestMapping("/api/admin/sections")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SectionController {

    private static final String XLSX_MIME =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final SectionService sectionService;

    // ── CRUD ────────────────────────────────────────────────────────────────

    /** POST /api/admin/sections */
    @PostMapping
    public ResponseEntity<ApiResponse<SectionDTO>> createSection(
            @Valid @RequestBody SectionDTO dto) {

        SectionDTO data = sectionService.createSection(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Section created successfully", data));
    }

    /** GET /api/admin/sections */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SectionDTO>>> getAllSections() {

        List<SectionDTO> data = sectionService.getAllSections();

        return ResponseEntity.ok(
                ApiResponse.success("Sections retrieved successfully", data));
    }

    /** GET /api/admin/sections/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SectionDTO>> getSectionById(
            @PathVariable UUID id) {

        SectionDTO data = sectionService.getSectionById(id);

        return ResponseEntity.ok(
                ApiResponse.success("Section retrieved successfully", data));
    }

    /** GET /api/admin/sections/semester/{semesterId} */
    @GetMapping("/semester/{semesterId}")
    public ResponseEntity<ApiResponse<List<SectionDTO>>> getSectionsBySemester(
            @PathVariable UUID semesterId) {

        List<SectionDTO> data = sectionService.getSectionsBySemesterId(semesterId);

        return ResponseEntity.ok(
                ApiResponse.success("Sections retrieved successfully", data));
    }

    /** PUT /api/admin/sections/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SectionDTO>> updateSection(
            @PathVariable UUID id,
            @Valid @RequestBody SectionDTO dto) {

        SectionDTO data = sectionService.updateSection(id, dto);

        return ResponseEntity.ok(
                ApiResponse.success("Section updated successfully", data));
    }

    /** DELETE /api/admin/sections/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSection(
            @PathVariable UUID id) {

        sectionService.deleteSection(id);

        return ResponseEntity.ok(
                ApiResponse.success("Section deleted successfully"));
    }

    // ── Excel ───────────────────────────────────────────────────────────────

    /** GET /api/admin/sections/excel/export */
    @GetMapping("/excel/export")
    public ResponseEntity<InputStreamResource> exportToExcel() {

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(XLSX_MIME))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"sections.xlsx\"")
                .body(new InputStreamResource(sectionService.exportToExcel()));
    }

    /** GET /api/admin/sections/excel/template */
    @GetMapping("/excel/template")
    public ResponseEntity<InputStreamResource> downloadTemplate() {

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(XLSX_MIME))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"sections_template.xlsx\"")
                .body(new InputStreamResource(sectionService.downloadTemplate()));
    }

    /** POST /api/admin/sections/excel/import */
    @PostMapping(value = "/excel/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<SectionDTO>>> importFromExcel(
            @RequestParam("file") MultipartFile file) {

        List<SectionDTO> data = sectionService.importFromExcel(file);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201,
                        data.size() + " section(s) imported successfully", data));
    }
}