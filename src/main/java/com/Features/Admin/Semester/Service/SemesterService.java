package com.Features.Admin.Semester.Service;

import com.Features.Admin.Semester.DTO.SemesterDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;

public interface SemesterService {

    SemesterDTO createSemester(SemesterDTO dto);

    SemesterDTO updateSemester(UUID id, SemesterDTO dto);

    SemesterDTO getSemesterById(UUID id);

    void deleteSemester(UUID id);

    List<SemesterDTO> getAllSemesters();

    // Excel
    ByteArrayInputStream exportToExcel();

    ByteArrayInputStream downloadTemplate();

    List<SemesterDTO> importFromExcel(MultipartFile file);
}