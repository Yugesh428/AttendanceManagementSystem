package com.Features.Admin.Semester.Repository;

import com.Features.Admin.Semester.Semester;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SemesterRepository extends JpaRepository<Semester, UUID> {
}