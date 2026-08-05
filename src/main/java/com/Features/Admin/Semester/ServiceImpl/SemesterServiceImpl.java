package com.Features.Admin.Semester.ServiceImpl;

import com.Features.Admin.Semester.DTO.SemesterDTO;
import com.Features.Admin.Semester.Repository.SemesterRepository;
import com.Features.Admin.Semester.Semester;
import com.Features.Admin.Semester.Service.SemesterService;
import com.Features.Admin.Semester.excel.SemesterExcelHelper;
import com.exception.ExcelImportException;
import com.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SemesterServiceImpl implements SemesterService {

    private final SemesterRepository semesterRepository;

    // ── CREATE ─────────────────────────────────────────────
    @Override
    public SemesterDTO createSemester(SemesterDTO dto) {

        log.info("[SEMESTER] Creating name='{}'", dto.getName());

        Semester saved = semesterRepository.save(
                Semester.builder()
                        .name(dto.getName())
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        log.info("[SEMESTER] Created id='{}'", saved.getId());

        return mapToDTO(saved);
    }

    // ── UPDATE ─────────────────────────────────────────────
    @Override
    public SemesterDTO updateSemester(UUID id, SemesterDTO dto) {

        log.info("[SEMESTER] Updating id='{}'", id);

        Semester semester = semesterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Semester", "id", id));

        semester.setName(dto.getName());

        return mapToDTO(semesterRepository.save(semester));
    }

    // ── GET BY ID ──────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public SemesterDTO getSemesterById(UUID id) {

        return mapToDTO(semesterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Semester", "id", id)));
    }

    // ── DELETE ─────────────────────────────────────────────
    @Override
    public void deleteSemester(UUID id) {

        log.info("[SEMESTER] Deleting id='{}'", id);

        if (!semesterRepository.existsById(id)) {
            throw new ResourceNotFoundException("Semester", "id", id);
        }

        semesterRepository.deleteById(id);
    }

    // ── GET ALL ────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<SemesterDTO> getAllSemesters() {

        return semesterRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ── EXPORT EXCEL ───────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public ByteArrayInputStream exportToExcel() {

        log.info("[SEMESTER] Exporting all semesters to Excel");

        List<SemesterDTO> list = semesterRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return SemesterExcelHelper.export(list);
    }

    // ── TEMPLATE ───────────────────────────────────────────
    @Override
    public ByteArrayInputStream downloadTemplate() {
        return SemesterExcelHelper.template();
    }

    // ── IMPORT EXCEL ───────────────────────────────────────
    @Override
    public List<SemesterDTO> importFromExcel(MultipartFile file) {

        if (!SemesterExcelHelper.hasExcelFormat(file)) {
            throw new ExcelImportException("Invalid file type. Please upload a .xlsx file.");
        }

        try {
            List<SemesterDTO> parsed = SemesterExcelHelper.parseExcel(file.getInputStream());

            log.info("[SEMESTER] Importing {} row(s)", parsed.size());

            List<SemesterDTO> savedList = new ArrayList<>();

            for (SemesterDTO dto : parsed) {

                Semester semester = semesterRepository.save(
                        Semester.builder()
                                .name(dto.getName())
                                .createdAt(LocalDateTime.now())
                                .build()
                );

                savedList.add(mapToDTO(semester));
            }

            return savedList;

        } catch (ExcelImportException e) {
            throw e;
        } catch (IOException e) {
            throw new ExcelImportException("Error reading Excel file: " + e.getMessage());
        }
    }

    // ── MAPPER ─────────────────────────────────────────────
    private SemesterDTO mapToDTO(Semester s) {
        return SemesterDTO.builder()
                .id(s.getId())
                .name(s.getName())
                .createdAt(s.getCreatedAt())
                .build();
    }
}