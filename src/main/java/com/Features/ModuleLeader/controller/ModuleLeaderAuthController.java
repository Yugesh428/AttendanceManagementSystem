package com.Features.ModuleLeader.controller;

import com.Features.ModuleLeader.dto.ModuleLeaderLoginRequest;
import com.Features.ModuleLeader.dto.ModuleLeaderLoginResponse;
import com.Features.ModuleLeader.service.ModuleLeaderService;
import com.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/module-leader")
@RequiredArgsConstructor
public class ModuleLeaderAuthController {

    private final ModuleLeaderService moduleLeaderService;

    /**
     * POST /api/auth/module-leader/login
     * Public — no JWT required.
     *
     * Body: { "email": "...", "password": "..." }
     * Response: JWT (ROLE_MODULE_LEADER) + subject details
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<ModuleLeaderLoginResponse>> login(
            @Valid @RequestBody ModuleLeaderLoginRequest request) {
        ModuleLeaderLoginResponse data = moduleLeaderService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", data));
    }
}
