package com.config;

import com.Features.SuperAdmin.Repository.SuperAdminRepository;
import com.Features.SuperAdmin.model.SuperAdmin;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class SuperAdminSeeder implements CommandLineRunner {

    private final SuperAdminRepository superAdminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.superadmin.email}")
    private String email;

    @Value("${app.superadmin.password}")
    private String rawPassword;

    @Value("${app.superadmin.firstname}")
    private String firstName;

    @Value("${app.superadmin.lastname}")
    private String lastName;

    @Override
    public void run(String... args) {
        if (superAdminRepository.existsByEmail(email)) {
            log.info("SuperAdmin seed: already exists — skipping.");
            return;
        }

        SuperAdmin superAdmin = SuperAdmin.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .active(true)
                .build();

        superAdminRepository.save(superAdmin);
        log.info("SuperAdmin seed: created default SuperAdmin with email '{}'", email);
    }
}
