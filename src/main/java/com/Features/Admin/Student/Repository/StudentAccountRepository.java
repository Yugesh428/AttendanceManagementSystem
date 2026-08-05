package com.Features.Admin.Student.repository;

import com.Features.Admin.Student.model.StudentAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentAccountRepository extends JpaRepository<StudentAccount, UUID> {
    Optional<StudentAccount> findByUsername(String username);
    boolean existsByUsername(String username);
}
