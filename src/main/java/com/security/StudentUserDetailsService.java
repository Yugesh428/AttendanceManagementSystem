package com.security;

import com.Features.Admin.Student.model.StudentAccount;
import com.Features.Admin.Student.repository.StudentAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Loads StudentAccount by email (username) for Spring Security authentication.
 * Mirrors TeacherUserDetailsService exactly — same pattern, different repository.
 */
@Service
@RequiredArgsConstructor
public class StudentUserDetailsService implements UserDetailsService {

    private final StudentAccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        StudentAccount account = accountRepository.findByUsername(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Student account not found with email: " + email));

        return User.builder()
                .username(account.getUsername())
                .password(account.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .disabled(!account.isActive())
                .build();
    }
}
