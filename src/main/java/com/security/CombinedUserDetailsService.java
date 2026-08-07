package com.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Resolves any login email across all four roles:
 *   1. SuperAdmin
 *   2. Admin
 *   3. Teacher
 *   4. Student
 *
 * Used by the single DaoAuthenticationProvider so one AuthenticationManager
 * handles all roles without multiple providers.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CombinedUserDetailsService implements UserDetailsService {

    private final SuperAdminUserDetailsService superAdminUDS;
    private final AdminUserDetailsService      adminUDS;
    private final TeacherUserDetailsService    teacherUDS;
    private final StudentUserDetailsService    studentUDS;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // 1. Try SuperAdmin
        try {
            log.debug("[AUTH] Trying SuperAdmin for '{}'", email);
            return superAdminUDS.loadUserByUsername(email);
        } catch (UsernameNotFoundException ignored) {}

        // 2. Try Admin
        try {
            log.debug("[AUTH] Trying Admin for '{}'", email);
            return adminUDS.loadUserByUsername(email);
        } catch (UsernameNotFoundException ignored) {}

        // 3. Try Teacher
        try {
            log.debug("[AUTH] Trying Teacher for '{}'", email);
            return teacherUDS.loadUserByUsername(email);
        } catch (UsernameNotFoundException ignored) {}

        // 4. Try Student
        log.debug("[AUTH] Trying Student for '{}'", email);
        return studentUDS.loadUserByUsername(email);
        // If none found → throws UsernameNotFoundException
        // → Spring Security converts to BadCredentialsException → 401
    }
}
