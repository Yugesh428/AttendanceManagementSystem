package com.Features.ModuleLeader.controller;

import com.Features.ModuleLeader.dto.CreateModuleLeaderRequest;
import com.Features.ModuleLeader.dto.ModuleLeaderResponse;
import com.Features.ModuleLeader.service.ModuleLeaderService;
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
@RequestMapping("/api/admin/module-leaders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminModuleLeaderController {

    private final ModuleLeaderService moduleLeaderService;

    /**
     * POST /api/admin/module-leaders
     * Create a new Module Leader and assign them to a subject.
     * Login credentials are auto-generated and emailed to them.
     *
     * Body:
     * {
     *   "firstName": "...",
     *   "lastName":  "...",
     *   "email":     "...",
     *   "phone":     "...",
     *   "subjectId": "<uuid>"
     * }
     *
     * Response includes generatedPassword — shown ONCE, emailed automatically.
     * Errors: 409 if email or subject already has a module leader.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ModuleLeaderResponse>> create(
            @Valid @RequestBody CreateModuleLeaderRequest request) {
        ModuleLeaderResponse data = moduleLeaderService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201,
                        "Module leader created. Credentials emailed to " + data.getEmail(), data));
    }

    /**
     * GET /api/admin/module-leaders
     * List all module leaders with their assigned subjects.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ModuleLeaderResponse>>> getAll() {
        return ResponseEntity.ok(
                ApiResponse.success("Module leaders retrieved", moduleLeaderService.getAll()));
    }

    /**
     * GET /api/admin/module-leaders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ModuleLeaderResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.success("Module leader retrieved", moduleLeaderService.getById(id)));
    }

    /**
     * PUT /api/admin/module-leaders/{id}
     * Update module leader details or reassign to a different subject.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ModuleLeaderResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateModuleLeaderRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Module leader updated", moduleLeaderService.update(id, request)));
    }

    /**
     * DELETE /api/admin/module-leaders/{id}
     * Remove module leader and their login account.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        moduleLeaderService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Module leader deleted"));
    }
}
