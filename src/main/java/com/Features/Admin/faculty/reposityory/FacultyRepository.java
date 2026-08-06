package com.Features.Admin.faculty.reposityory;

import com.Features.Admin.faculty.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FacultyRepository extends JpaRepository<Faculty, UUID> {

    // 🔍 Find by Email (useful for uniqueness check)
    Optional<Faculty> findByEmail(String email);

    // 🔍 Check if email already exists
    boolean existsByEmail(String email);

    // 🔍 Find by Phone
    Optional<Faculty> findByPhone(String phone);

    // 🔍 Custom search (optional)
    // List<Faculty> findByFirstNameContainingIgnoreCase(String name);
}