package com.Features.Admin.AdminPart.Controller;

import com.Features.Admin.AdminPart.Service.AdminService;
import com.Features.Admin.AdminPart.dto.AdminResponse;
import com.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    /**
     * GET /api/admin/me
     * Returns the profile of the currently authenticated admin.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AdminResponse>> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        AdminResponse data = adminService.getProfile(userDetails.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("Profile retrieved successfully", data));
    }
}
