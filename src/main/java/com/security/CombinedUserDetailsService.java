package com.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Resolves any login email across all five roles:
 *   1. SuperAdmin
 *   2. Admin
 *   3. Teacher
 *   4. Student
 *   5. Module Leader
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CombinedUserDetailsService implements UserDetailsService {

    private final SuperAdminUserDetailsService    superAdminUDS;
    private final AdminUserDetailsService         adminUDS;
    private final TeacherUserDetailsService       teacherUDS;
    private final StudentUserDetailsService       studentUDS;
    private final ModuleLeaderUserDetailsService  moduleLeaderUDS;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        try { return superAdminUDS.loadUserByUsername(email); }
        catch (UsernameNotFoundException ignored) {}

        try { return adminUDS.loadUserByUsername(email); }
        catch (UsernameNotFoundException ignored) {}

        try { return teacherUDS.loadUserByUsername(email); }
        catch (UsernameNotFoundException ignored) {}

        try { return studentUDS.loadUserByUsername(email); }
        catch (UsernameNotFoundException ignored) {}

        // 5. Try Module Leader
        log.debug("[AUTH] Trying Module Leader for '{}'", email);
        return moduleLeaderUDS.loadUserByUsername(email);
    }
}
