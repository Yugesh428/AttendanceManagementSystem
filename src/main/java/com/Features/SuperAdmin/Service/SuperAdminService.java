package com.Features.SuperAdmin.Service;

import com.Features.Admin.AdminPart.dto.AdminResponse;
import com.Features.Admin.AdminPart.dto.CreateAdminRequest;
import com.Features.SuperAdmin.dto.SuperAdminLoginRequest;
import com.Features.SuperAdmin.dto.SuperAdminLoginResponse;

import java.util.List;

public interface SuperAdminService {

    /**
     * Authenticate a SuperAdmin and return a JWT token.
     */
    SuperAdminLoginResponse login(SuperAdminLoginRequest request);

    /**
     * Create a new Admin under this SuperAdmin (by email of the authenticated SuperAdmin).
     */
    AdminResponse createAdmin(String superAdminEmail, CreateAdminRequest request);

    /**
     * List all Admins.
     */
    List<AdminResponse> getAllAdmins();
}
