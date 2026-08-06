package com.Features.Admin.Subject.repository;

import com.Features.Admin.Subject.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SubjectRepository extends JpaRepository<Subject, UUID> {

    // 🔍 Find by subject code (used for validation / uniqueness check)
    Optional<Subject> findBySubjectCode(String subjectCode);

    // 🔍 Check if subject code already exists
    boolean existsBySubjectCode(String subjectCode);
}