package com.security;

import com.Features.ModuleLeader.model.ModuleLeaderAccount;
import com.Features.ModuleLeader.repository.ModuleLeaderAccountRepository;
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
public class ModuleLeaderUserDetailsService implements UserDetailsService {

    private final ModuleLeaderAccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        ModuleLeaderAccount account = accountRepository.findByUsername(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Module leader account not found with email: " + email));

        return User.builder()
                .username(account.getUsername())
                .password(account.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_MODULE_LEADER")))
                .disabled(!account.isActive())
                .build();
    }
}
