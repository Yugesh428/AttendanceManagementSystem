package com.Features.Admin.AdminPart.ServiceImplementation;

import com.Features.Admin.AdminPart.Repository.AdminRepository;
import com.Features.Admin.AdminPart.Service.AdminService;
import com.Features.Admin.AdminPart.dto.AdminLoginRequest;
import com.Features.Admin.AdminPart.dto.AdminLoginResponse;
import com.Features.Admin.AdminPart.dto.AdminResponse;
import com.Features.Admin.AdminPart.model.Admin;
import com.Features.Admin.AdminPart.model.AdminStatus;
import com.exception.ResourceNotFoundException;
import com.exception.UnauthorizedException;
import com.security.AdminUserDetailsService;
import com.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final AdminUserDetailsService adminUserDetailsService;

    // ── Login ──────────────────────────────────────────────────────────────────
    @Override
    public AdminLoginResponse login(AdminLoginRequest request) {
        log.info("[ADMIN LOGIN] Attempt for email: {}", request.getEmail());

        // Check account exists first to give a clear error before Spring Security's generic one
        Admin admin = adminRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("[ADMIN LOGIN] No admin found for email: {}", request.getEmail());
                    return new UnauthorizedException("Invalid email or password");
                });

        // Check account is active
        if (admin.getStatus() == AdminStatus.INACTIVE || admin.getStatus() == AdminStatus.SUSPENDED) {
            log.warn("[ADMIN LOGIN] Account is {} for email: {}", admin.getStatus(), request.getEmail());
            throw new UnauthorizedException("Your account is " + admin.getStatus().name().toLowerCase()
                    + ". Please contact the administrator.");
        }

        // Authenticate password via Spring Security (throws BadCredentialsException if wrong)
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException ex) {
            log.warn("[ADMIN LOGIN] Wrong password for email: {}", request.getEmail());
            throw new UnauthorizedException("Invalid email or password");
        } catch (DisabledException ex) {
            throw new UnauthorizedException("Account is disabled. Contact the administrator.");
        }

        UserDetails userDetails = adminUserDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtUtils.generateToken(userDetails, "ROLE_ADMIN");

        log.info("[ADMIN LOGIN] Success for email: {}", request.getEmail());

        return AdminLoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .id(admin.getId())
                .firstName(admin.getFirstName())
                .lastName(admin.getLastName())
                .email(admin.getEmail())
                .tenantName(admin.getTenantName())
                .status(admin.getStatus())
                .role("ROLE_ADMIN")
                .build();
    }

    // ── Get Profile ────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public AdminResponse getProfile(String email) {
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", "email", email));
        return mapToResponse(admin);
    }

    // ── Mapper ─────────────────────────────────────────────────────────────────
    private AdminResponse mapToResponse(Admin admin) {
        return AdminResponse.builder()
                .id(admin.getId())
                .firstName(admin.getFirstName())
                .lastName(admin.getLastName())
                .email(admin.getEmail())
                .phoneNumber(admin.getPhoneNumber())
                .tenantName(admin.getTenantName())
                .organizationAddress(admin.getOrganizationAddress())
                .organizationCity(admin.getOrganizationCity())
                .organizationCountry(admin.getOrganizationCountry())
                .status(admin.getStatus())
                .createdAt(admin.getCreatedAt())
                .createdBySuperAdminId(admin.getCreatedBySuperAdminId())
                .build();
    }
}
