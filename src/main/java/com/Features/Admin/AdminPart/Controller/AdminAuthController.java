package com.Features.Admin.AdminPart.Controller;

import com.Features.Admin.AdminPart.Service.AdminService;
import com.Features.Admin.AdminPart.dto.AdminLoginRequest;
import com.Features.Admin.AdminPart.dto.AdminLoginResponse;
import com.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminService adminService;

    /**
     * POST /api/auth/admin/login
     * Public — use the email + password set by SuperAdmin when creating the account.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AdminLoginResponse>> login(
            @Valid @RequestBody AdminLoginRequest request) {
        AdminLoginResponse data = adminService.login(request);
        return ResponseEntity.ok(
                ApiResponse.success("Login successful", data));
    }
}
