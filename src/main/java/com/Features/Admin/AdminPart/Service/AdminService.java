package com.Features.Admin.AdminPart.Service;

import com.Features.Admin.AdminPart.dto.AdminLoginRequest;
import com.Features.Admin.AdminPart.dto.AdminLoginResponse;
import com.Features.Admin.AdminPart.dto.AdminResponse;

public interface AdminService {

    /**
     * Authenticate an Admin with email + password and return a JWT.
     */
    AdminLoginResponse login(AdminLoginRequest request);

    /**
     * Get the profile of the currently authenticated admin.
     */
    AdminResponse getProfile(String email);
}
