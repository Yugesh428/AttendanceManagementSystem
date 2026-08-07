package com.Features.Teacher.repository;

import com.Features.Teacher.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, UUID> {
    boolean existsByFacultyId(UUID facultyId);
    Optional<Teacher> findByFacultyId(UUID facultyId);
    Optional<Teacher> findByFacultyEmail(String email);
}
