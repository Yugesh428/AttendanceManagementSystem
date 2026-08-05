package com.Features.SuperAdmin.Controller;

import com.Features.Admin.AdminPart.dto.AdminResponse;
import com.Features.Admin.AdminPart.dto.CreateAdminRequest;
import com.Features.SuperAdmin.Service.SuperAdminService;
import com.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/superadmin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminController {

    private final SuperAdminService superAdminService;

    /**
     * POST /api/superadmin/admins
     * Create a new Admin (SuperAdmin only).
     */
    @PostMapping("/admins")
    public ResponseEntity<ApiResponse<AdminResponse>> createAdmin(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateAdminRequest request) {
        AdminResponse data = superAdminService.createAdmin(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Admin created successfully", data));
    }

    /**
     * GET /api/superadmin/admins
     * List all Admins (SuperAdmin only).
     */
    @GetMapping("/admins")
    public ResponseEntity<ApiResponse<List<AdminResponse>>> getAllAdmins() {
        List<AdminResponse> data = superAdminService.getAllAdmins();
        return ResponseEntity.ok(
                ApiResponse.success("Admins retrieved successfully", data));
    }
}
