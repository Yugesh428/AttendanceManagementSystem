package com.Features.Teacher.controller;

import com.Features.Teacher.dto.TeacherLoginRequest;
import com.Features.Teacher.dto.TeacherLoginResponse;
import com.Features.Teacher.service.TeacherService;
import com.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/teacher")
@RequiredArgsConstructor
public class TeacherAuthController {

    private final TeacherService teacherService;

    /**
     * POST /api/auth/teacher/login
     * Public — no JWT required.
     * Credentials are set by Admin when registering the teacher from Faculty.
     *
     * Request:
     * {
     *   "email":    "john.doe@institution.com",
     *   "password": "john@3210"
     * }
     *
     * Response: 200 + JWT (ROLE_TEACHER) + teacher details
     * Errors:   401 on bad credentials or inactive account
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TeacherLoginResponse>> login(
            @Valid @RequestBody TeacherLoginRequest request) {
        TeacherLoginResponse data = teacherService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", data));
    }
}
