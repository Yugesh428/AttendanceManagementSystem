package com.Features.Admin.Student.service;

import com.Features.Admin.Student.DTO.StudentDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;

public interface StudentService {

    // ── CRUD ────────────────────────────────────────────────────────────────
    StudentDTO createStudent(StudentDTO dto);

    StudentDTO updateStudent(UUID id, StudentDTO dto);

    StudentDTO findStudentById(UUID id);

    List<StudentDTO> findAllStudents();

    void deleteStudent(UUID id);

    // ── Excel ───────────────────────────────────────────────────────────────
    ByteArrayInputStream exportToExcel();

    ByteArrayInputStream downloadTemplate();

    List<StudentDTO> importFromExcel(MultipartFile file);
}
