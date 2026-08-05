package com.Features.Admin.Building.Controller;

import com.Features.Admin.Building.BuildingDTO.BuildingDTO;
import com.Features.Admin.Building.service.BuildingService;
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
@RequestMapping("/api/admin/buildings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class BuildingController {

    private static final String XLSX_MIME =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final BuildingService service;

    // ── CRUD ──────────────────────────────────────────────────────────────────

    /** POST /api/admin/buildings */
    @PostMapping
    public ResponseEntity<ApiResponse<BuildingDTO>> createBuilding(
            @Valid @RequestBody BuildingDTO dto) {
        BuildingDTO data = service.createBuildingDTO(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Building created successfully", data));
    }

    /** GET /api/admin/buildings */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BuildingDTO>>> getAllBuildings() {
        List<BuildingDTO> data = service.getAllBuildingDTOs();
        return ResponseEntity.ok(
                ApiResponse.success("Buildings retrieved successfully", data));
    }

    /** GET /api/admin/buildings/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BuildingDTO>> getBuildingById(@PathVariable UUID id) {
        BuildingDTO data = service.getBuildingDTOById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Building retrieved successfully", data));
    }

    /** PUT /api/admin/buildings/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BuildingDTO>> updateBuilding(
            @PathVariable UUID id,
            @Valid @RequestBody BuildingDTO dto) {
        BuildingDTO data = service.updateBuildingDTO(id, dto);
        return ResponseEntity.ok(
                ApiResponse.success("Building updated successfully", data));
    }

    /** DELETE /api/admin/buildings/{id} — 200 with message instead of bare 204 */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBuilding(@PathVariable UUID id) {
        service.deleteBuildingDTOById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Building deleted successfully"));
    }

    // ── Excel ─────────────────────────────────────────────────────────────────

    /**
     * GET /api/admin/buildings/excel/export
     * Downloads all buildings as .xlsx (binary — no ApiResponse wrapper).
     * Columns: ID | Name | Location | Created At
     */
    @GetMapping("/excel/export")
    public ResponseEntity<InputStreamResource> exportToExcel() {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(XLSX_MIME))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"buildings.xlsx\"")
                .body(new InputStreamResource(service.exportToExcel()));
    }

    /**
     * GET /api/admin/buildings/excel/template
     * Downloads blank import template.
     * Import columns: Name * | Location
     */
    @GetMapping("/excel/template")
    public ResponseEntity<InputStreamResource> downloadTemplate() {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(XLSX_MIME))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"buildings_template.xlsx\"")
                .body(new InputStreamResource(service.downloadTemplate()));
    }

    /**
     * POST /api/admin/buildings/excel/import
     * Bulk-create buildings from an .xlsx file.
     * Returns 201 + list of created buildings wrapped in ApiResponse.
     * Returns 422 on bad data with row-level error message.
     */
    @PostMapping(value = "/excel/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<BuildingDTO>>> importFromExcel(
            @RequestParam("file") MultipartFile file) {
        List<BuildingDTO> data = service.importFromExcel(file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201,
                        data.size() + " building(s) imported successfully", data));
    }
}
