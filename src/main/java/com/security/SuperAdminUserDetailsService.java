package com.security;

import com.Features.SuperAdmin.Repository.SuperAdminRepository;
import com.Features.SuperAdmin.model.SuperAdmin;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Loads SuperAdmin by email for Spring Security authentication.
 * Also supports Admin login by delegating to AdminUserDetailsService.
 */
@Service
@RequiredArgsConstructor
public class SuperAdminUserDetailsService implements UserDetailsService {

    private final SuperAdminRepository superAdminRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        SuperAdmin superAdmin = superAdminRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("SuperAdmin not found with email: " + email));

        return User.builder()
                .username(superAdmin.getEmail())
                .password(superAdmin.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")))
                .disabled(!superAdmin.isActive())
                .build();
    }
}
