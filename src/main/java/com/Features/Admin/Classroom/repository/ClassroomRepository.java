package com.Features.Admin.Classroom.repository;

import com.Features.Admin.Classroom.model.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClassroomRepository extends JpaRepository<Classroom, UUID> {

}
