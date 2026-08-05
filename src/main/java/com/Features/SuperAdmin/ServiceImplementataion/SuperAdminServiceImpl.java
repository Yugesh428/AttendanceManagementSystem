package com.Features.SuperAdmin.ServiceImplementataion;

import com.Features.Admin.AdminPart.Repository.AdminRepository;
import com.Features.Admin.AdminPart.dto.AdminResponse;
import com.Features.Admin.AdminPart.dto.CreateAdminRequest;
import com.Features.Admin.AdminPart.model.Admin;
import com.Features.SuperAdmin.Repository.SuperAdminRepository;
import com.Features.SuperAdmin.Service.SuperAdminService;
import com.Features.SuperAdmin.dto.SuperAdminLoginRequest;
import com.Features.SuperAdmin.dto.SuperAdminLoginResponse;
import com.Features.SuperAdmin.model.SuperAdmin;
import com.exception.DuplicateResourceException;
import com.exception.ResourceNotFoundException;
import com.exception.UnauthorizedException;
import com.security.JwtUtils;
import com.security.SuperAdminUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SuperAdminServiceImpl implements SuperAdminService {

    private final SuperAdminRepository superAdminRepository;
    private final AdminRepository adminRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final SuperAdminUserDetailsService superAdminUserDetailsService;

    // ── Login ──────────────────────────────────────────────────────────────────
    @Override
    public SuperAdminLoginResponse login(SuperAdminLoginRequest request) {
        log.info("[SUPER ADMIN LOGIN] Attempt for email: {}", request.getEmail());

        // Explicit lookup so we can give a clear "not found" vs "wrong password" distinction in logs
        SuperAdmin superAdmin = superAdminRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("[SUPER ADMIN LOGIN] No super admin found for email: {}", request.getEmail());
                    // Return 401 (not 404) so we don't leak whether the email exists
                    return new UnauthorizedException("Invalid email or password");
                });

        if (!superAdmin.isActive()) {
            log.warn("[SUPER ADMIN LOGIN] Account inactive for email: {}", request.getEmail());
            throw new UnauthorizedException("Super admin account is disabled.");
        }

        // Delegate password check to Spring Security
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException ex) {
            log.warn("[SUPER ADMIN LOGIN] Wrong password for email: {}", request.getEmail());
            throw new UnauthorizedException("Invalid email or password");
        }

        UserDetails userDetails = superAdminUserDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtUtils.generateToken(userDetails, "ROLE_SUPER_ADMIN");

        log.info("[SUPER ADMIN LOGIN] Success for email: {}", request.getEmail());

        return SuperAdminLoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .id(superAdmin.getId())
                .firstName(superAdmin.getFirstName())
                .lastName(superAdmin.getLastName())
                .email(superAdmin.getEmail())
                .role("ROLE_SUPER_ADMIN")
                .build();
    }

    // ── Create Admin ───────────────────────────────────────────────────────────
    @Override
    public AdminResponse createAdmin(String superAdminEmail, CreateAdminRequest request) {
        log.info("[CREATE ADMIN] SuperAdmin '{}' creating admin with email '{}'",
                superAdminEmail, request.getEmail());

        // 409 if email already taken
        if (adminRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Admin", "email", request.getEmail());
        }

        SuperAdmin superAdmin = superAdminRepository.findByEmail(superAdminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("SuperAdmin", "email", superAdminEmail));

        Admin admin = Admin.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .tenantName(request.getTenantName())
                .organizationAddress(request.getOrganizationAddress())
                .organizationCity(request.getOrganizationCity())
                .organizationCountry(request.getOrganizationCountry())
                .createdBySuperAdminId(superAdmin.getId())
                .build();

        Admin saved = adminRepository.save(admin);
        log.info("[CREATE ADMIN] Admin created with id: {}", saved.getId());

        return mapToAdminResponse(saved);
    }

    // ── Get All Admins ─────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<AdminResponse> getAllAdmins() {
        return adminRepository.findAll()
                .stream()
                .map(this::mapToAdminResponse)
                .collect(Collectors.toList());
    }

    // ── Mapper ─────────────────────────────────────────────────────────────────
    private AdminResponse mapToAdminResponse(Admin admin) {
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
