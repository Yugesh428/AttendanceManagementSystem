package com.Features.Student.serviceImpl;

import com.Features.Admin.Student.model.Student;
import com.Features.Admin.Student.model.StudentAccount;
import com.Features.Admin.Student.repository.StudentAccountRepository;
import com.Features.Student.dto.StudentLoginRequest;
import com.Features.Student.dto.StudentLoginResponse;
import com.Features.Student.model.DeviceChangeLog;
import com.Features.Student.repository.DeviceChangeLogRepository;
import com.Features.Student.service.StudentAuthService;
import com.common.EmailService;
import com.exception.AppException;
import com.exception.ResourceNotFoundException;
import com.exception.UnauthorizedException;
import com.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StudentAuthServiceImpl implements StudentAuthService {

    private final StudentAccountRepository accountRepository;
    private final DeviceChangeLogRepository deviceChangeLogRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final EmailService emailService;

    // ══════════════════════════════════════════════════════════════════════════
    // LOGIN
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public StudentLoginResponse login(StudentLoginRequest request,
                                      String userAgent,
                                      String deviceId) {

        log.info("[STUDENT LOGIN] Attempt email='{}' isMobile={} deviceId='{}'",
                request.getEmail(), isMobile(userAgent), deviceId);

        // ── 1. Load account ────────────────────────────────────────────────────
        StudentAccount account = accountRepository.findByUsername(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("[STUDENT LOGIN] No account found for email='{}'", request.getEmail());
                    return new UnauthorizedException("Invalid email or password");
                });

        if (!account.isActive()) {
            log.warn("[STUDENT LOGIN] Account inactive for email='{}'", request.getEmail());
            throw new UnauthorizedException("Your account is inactive. Contact the administrator.");
        }

        // ── 2. Phone-first enforcement ─────────────────────────────────────────
        // BYPASSED for development/testing — re-enable for production
        // if (!account.isPhoneRegistered() && !isMobile(userAgent)) {
        //     log.warn("[STUDENT LOGIN] Non-mobile first login rejected for email='{}'",
        //             request.getEmail());
        //     throw new AppException(HttpStatus.FORBIDDEN,
        //             "First login must be from your mobile phone browser. "
        //             + "Please log in from your phone to register your device first.");
        // }

        // ── 3. Authenticate password ───────────────────────────────────────────
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(), request.getPassword()));
        } catch (BadCredentialsException ex) {
            log.warn("[STUDENT LOGIN] Wrong password for email='{}'", request.getEmail());
            throw new UnauthorizedException("Invalid email or password");
        }

        // ── 4. Device handling ─────────────────────────────────────────────────
        boolean deviceJustRegistered = false;
        boolean deviceChanged        = false;

        if (deviceId != null && !deviceId.isBlank()) {

            if (!account.isPhoneRegistered()) {
                // ── CASE A: First phone login — register device ─────────────────
                log.info("[STUDENT LOGIN] Registering phone deviceId='{}' for email='{}'",
                        deviceId, request.getEmail());
                account.setDeviceId(deviceId);
                account.setDeviceRegisteredAt(LocalDateTime.now());
                account.setPhoneRegistered(true);
                accountRepository.save(account);
                deviceJustRegistered = true;

            } else if (!deviceId.equals(account.getDeviceId())) {
                // ── CASE B: Different device detected — log + alert admin ────────
                log.warn("[STUDENT LOGIN] Device mismatch for email='{}' old='{}' new='{}'",
                        request.getEmail(), account.getDeviceId(), deviceId);

                // Log the change
                deviceChangeLogRepository.save(DeviceChangeLog.builder()
                        .student(account.getStudent())
                        .oldDeviceId(account.getDeviceId())
                        .newDeviceId(deviceId)
                        .build());

                deviceChanged = true;

                // Notify admin asynchronously (never blocks login)
                Student student = account.getStudent();
                String studentName = student.getFirstName()
                        + (student.getLastName() != null ? " " + student.getLastName() : "");
                // TODO: replace with actual admin email lookup when multi-admin support is needed
                // For now email goes to a configured address — wire via @Value if needed
                emailService.sendDeviceChangeAlert(
                        "admin@attendance.com",  // placeholder — replace with actual admin email
                        studentName,
                        student.getEmail(),
                        account.getDeviceId(),
                        deviceId);

            }
            // CASE C: Same device as registered — no action needed
        }

        // ── 5. Generate JWT ────────────────────────────────────────────────────
        UserDetails userDetails = User.builder()
                .username(account.getUsername())
                .password(account.getPassword())
                .authorities(new SimpleGrantedAuthority("ROLE_STUDENT"))
                .build();

        String token = jwtUtils.generateToken(userDetails, "ROLE_STUDENT");

        Student student = account.getStudent();
        log.info("[STUDENT LOGIN] Success for email='{}'", request.getEmail());

        return StudentLoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .studentId(student.getId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .email(student.getEmail())
                .phoneRegistered(account.isPhoneRegistered())
                .deviceJustRegistered(deviceJustRegistered)
                .deviceChanged(deviceChanged)
                .role("ROLE_STUDENT")
                .build();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Detects a mobile browser from the User-Agent string.
     * Covers Android, iPhone, iPad (modern), Windows Phone, and generic Mobile.
     * Not foolproof — can be spoofed — but sufficient for soft enforcement.
     */
    private boolean isMobile(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) return false;
        String ua = userAgent.toLowerCase();
        return ua.contains("android")
                || ua.contains("iphone")
                || ua.contains("ipad")
                || ua.contains("mobile")
                || ua.contains("windows phone");
    }
}
