package com.Features.Student.service;

import com.Features.Student.dto.StudentLoginRequest;
import com.Features.Student.dto.StudentLoginResponse;

public interface StudentAuthService {

    /**
     * Authenticates a student and handles device registration / change detection.
     *
     * @param request    email + password
     * @param userAgent  HTTP User-Agent header — used to enforce phone-first rule
     * @param deviceId   X-Device-Id header — browser fingerprint from localStorage
     * @return JWT + student info + device registration flags
     */
    StudentLoginResponse login(StudentLoginRequest request,
                               String userAgent,
                               String deviceId);
}
