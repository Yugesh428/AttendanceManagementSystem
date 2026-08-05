package com.Features.Admin.Section.Service;

import com.Features.Admin.Section.DTO.SectionDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;

public interface SectionService {

    // ── CRUD ────────────────────────────────────────────────────────────────
    SectionDTO createSection(SectionDTO dto);

    SectionDTO updateSection(UUID id, SectionDTO dto);

    SectionDTO getSectionById(UUID id);

    List<SectionDTO> getAllSections();

    void deleteSection(UUID id);

    List<SectionDTO> getSectionsBySemesterId(UUID semesterId);

    // ── Excel ───────────────────────────────────────────────────────────────
    ByteArrayInputStream exportToExcel();

    ByteArrayInputStream downloadTemplate();

    List<SectionDTO> importFromExcel(MultipartFile file);
}