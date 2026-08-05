package com.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Tries SuperAdmin first, then Admin.
 * Used by the AuthenticationManager so a single DaoAuthenticationProvider
 * can authenticate both roles.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CombinedUserDetailsService implements UserDetailsService {

    private final SuperAdminUserDetailsService superAdminUDS;
    private final AdminUserDetailsService adminUDS;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Try SuperAdmin first
        try {
            log.debug("[AUTH] Trying SuperAdmin lookup for '{}'", email);
            return superAdminUDS.loadUserByUsername(email);
        } catch (UsernameNotFoundException ignored) {
            log.debug("[AUTH] Not a SuperAdmin, trying Admin for '{}'", email);
        }

        // Fall back to Admin
        return adminUDS.loadUserByUsername(email);
        // If neither found, AdminUserDetailsService throws UsernameNotFoundException
        // which Spring Security converts to BadCredentialsException automatically.
    }
}
