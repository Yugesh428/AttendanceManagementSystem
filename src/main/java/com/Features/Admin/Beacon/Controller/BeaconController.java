package com.Features.Admin.Beacon.Controller;

import com.Features.Admin.Beacon.DTO.BeaconDTO;
import com.Features.Admin.Beacon.Service.BeaconService;
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
@RequestMapping("/api/admin/beacons")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class BeaconController {

    private static final String XLSX_MIME =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final BeaconService beaconService;

    // ── CRUD ──────────────────────────────────────────────────────────────────

    /** POST /api/admin/beacons */
    @PostMapping
    public ResponseEntity<ApiResponse<BeaconDTO>> createBeacon(
            @Valid @RequestBody BeaconDTO dto) {
        BeaconDTO data = beaconService.createBeacon(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Beacon created successfully", data));
    }

    /** GET /api/admin/beacons/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BeaconDTO>> getBeaconById(@PathVariable UUID id) {
        BeaconDTO data = beaconService.getBeaconById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Beacon retrieved successfully", data));
    }

    /** GET /api/admin/beacons/classroom/{classroomId} */
    @GetMapping("/classroom/{classroomId}")
    public ResponseEntity<ApiResponse<List<BeaconDTO>>> getBeaconsByClassroom(
            @PathVariable UUID classroomId) {
        List<BeaconDTO> data = beaconService.getBeaconsByClassroomId(classroomId);
        return ResponseEntity.ok(
                ApiResponse.success("Beacons retrieved successfully", data));
    }

    /** PUT /api/admin/beacons/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BeaconDTO>> updateBeacon(
            @PathVariable UUID id,
            @Valid @RequestBody BeaconDTO dto) {
        BeaconDTO data = beaconService.updateBeacon(id, dto);
        return ResponseEntity.ok(
                ApiResponse.success("Beacon updated successfully", data));
    }

    /** DELETE /api/admin/beacons/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBeacon(@PathVariable UUID id) {
        beaconService.deleteBeaconById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Beacon deleted successfully"));
    }

    // ── Excel ─────────────────────────────────────────────────────────────────

    /**
     * GET /api/admin/beacons/excel/export
     * Downloads all beacons as .xlsx (binary).
     * Columns: ID | UUID | Major | Minor | Classroom ID | Classroom Name | Created At
     */
    @GetMapping("/excel/export")
    public ResponseEntity<InputStreamResource> exportToExcel() {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(XLSX_MIME))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"beacons.xlsx\"")
                .body(new InputStreamResource(beaconService.exportToExcel()));
    }

    /**
     * GET /api/admin/beacons/excel/template
     * Downloads blank import template.
     * Import columns: UUID * | Major * | Minor * | Classroom ID *
     */
    @GetMapping("/excel/template")
    public ResponseEntity<InputStreamResource> downloadTemplate() {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(XLSX_MIME))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"beacons_template.xlsx\"")
                .body(new InputStreamResource(beaconService.downloadTemplate()));
    }

    /**
     * POST /api/admin/beacons/excel/import
     * Bulk-create beacons from an .xlsx file.
     * Returns 201 + list of created beacons wrapped in ApiResponse.
     * Returns 422 on bad data — message includes exact row number and field.
     */
    @PostMapping(value = "/excel/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<BeaconDTO>>> importFromExcel(
            @RequestParam("file") MultipartFile file) {
        List<BeaconDTO> data = beaconService.importFromExcel(file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201,
                        data.size() + " beacon(s) imported successfully", data));
    }
}
