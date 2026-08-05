package com.Features.Admin.Department.service;

import com.Features.Admin.Department.DTO.DepartmentDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;

public interface DepartmentService {

    DepartmentDTO createDepartmentDTO(DepartmentDTO dto);

    DepartmentDTO getDepartmentDTOById(UUID id);

    List<DepartmentDTO> getAllDepartmentDTOs();

    DepartmentDTO updateDepartmentDTO(UUID id, DepartmentDTO dto);

    void deleteDepartmentDTOById(UUID id);

    // ── Excel ──────────────────────────────────────────────────────────────────
    ByteArrayInputStream exportToExcel();

    ByteArrayInputStream downloadTemplate();

    List<DepartmentDTO> importFromExcel(MultipartFile file);
}