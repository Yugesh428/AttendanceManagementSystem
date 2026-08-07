package com.Features.Teacher.repository;

import com.Features.Teacher.model.TeacherAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeacherAccountRepository extends JpaRepository<TeacherAccount, UUID> {
    Optional<TeacherAccount> findByUsername(String username);
    boolean existsByUsername(String username);
}
