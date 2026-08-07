package com.Features.Teacher.controller;

import com.Features.Teacher.dto.TeacherDTO;
import com.Features.Teacher.service.TeacherService;
import com.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherController {

    private final TeacherService teacherService;

    /**
     * GET /api/teacher/me
     * Returns the profile of the currently authenticated teacher.
     * Requires: Authorization: Bearer <teacher-jwt>
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<TeacherDTO>> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        TeacherDTO data = teacherService.getMyProfile(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", data));
    }
}
