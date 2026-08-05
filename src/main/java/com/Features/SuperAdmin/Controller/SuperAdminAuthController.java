package com.Features.SuperAdmin.Controller;

import com.Features.SuperAdmin.Service.SuperAdminService;
import com.Features.SuperAdmin.dto.SuperAdminLoginRequest;
import com.Features.SuperAdmin.dto.SuperAdminLoginResponse;
import com.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/superadmin")
@RequiredArgsConstructor
public class SuperAdminAuthController {

    private final SuperAdminService superAdminService;

    /**
     * POST /api/auth/superadmin/login
     * Public — no JWT required.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<SuperAdminLoginResponse>> login(
            @Valid @RequestBody SuperAdminLoginRequest request) {
        SuperAdminLoginResponse data = superAdminService.login(request);
        return ResponseEntity.ok(
                ApiResponse.success("Login successful", data));
    }
}
