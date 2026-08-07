package com.Features.Admin.Student.repository;

import com.Features.Admin.Student.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {

    boolean existsByEmail(String email);

    Optional<Student> findByEmail(String email);
}
