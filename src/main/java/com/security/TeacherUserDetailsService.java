package com.security;

import com.Features.Teacher.model.TeacherAccount;
import com.Features.Teacher.repository.TeacherAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherUserDetailsService implements UserDetailsService {

    private final TeacherAccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        TeacherAccount account = accountRepository.findByUsername(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Teacher account not found with email: " + email));

        return User.builder()
                .username(account.getUsername())
                .password(account.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_TEACHER")))
                .disabled(!account.isActive())
                .build();
    }
}
