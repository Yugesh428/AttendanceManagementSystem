package com.Features.Admin.Section.ServiceImpl;

import com.Features.Admin.Section.DTO.SectionDTO;
import com.Features.Admin.Section.Section;
import com.Features.Admin.Section.Repository.SectionRepository;
import com.Features.Admin.Section.Service.SectionService;
import com.Features.Admin.Section.excel.SectionExcelHelper;
import com.Features.Admin.Semester.Semester;
import com.Features.Admin.Semester.Repository.SemesterRepository;
import com.exception.DuplicateResourceException;
import com.exception.ResourceNotFoundException;
import com.exception.ExcelImportException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SectionServiceImpl implements SectionService {

    private final SectionRepository sectionRepository;
    private final SemesterRepository semesterRepository;

    // ── CREATE ────────────────────────────────────────────────────────────────
    @Override
    public SectionDTO createSection(SectionDTO dto) {
        log.info("[SECTION] Creating name='{}' semesterId='{}'", dto.getName(), dto.getSemesterId());

        Semester semester = findSemester(dto.getSemesterId());

        if (sectionRepository.existsByNameAndSemesterId(dto.getName(), dto.getSemesterId())) {
            throw new DuplicateResourceException("Section", "name in semester", dto.getName());
        }

        Section saved = sectionRepository.save(Section.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .capacity(dto.getCapacity())
                .semester(semester)
                .build());

        log.info("[SECTION] Created id='{}'", saved.getId());
        return mapToDTO(saved);
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────
    @Override
    public SectionDTO updateSection(UUID id, SectionDTO dto) {
        log.info("[SECTION] Updating id='{}'", id);

        Section section = findSection(id);
        Semester semester = findSemester(dto.getSemesterId());

        // Duplicate check — allow same name if it belongs to this same section
        if (!section.getName().equalsIgnoreCase(dto.getName())
                && sectionRepository.existsByNameAndSemesterId(dto.getName(), dto.getSemesterId())) {
            throw new DuplicateResourceException("Section", "name in semester", dto.getName());
        }

        section.setName(dto.getName());
        section.setDescription(dto.getDescription());
        section.setCapacity(dto.getCapacity());
        section.setSemester(semester);

        return mapToDTO(sectionRepository.save(section));
    }

    // ── GET BY ID ─────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public SectionDTO getSectionById(UUID id) {
        return mapToDTO(findSection(id));
    }

    // ── GET ALL ───────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<SectionDTO> getAllSections() {
        return sectionRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
    @Override
    public void deleteSection(UUID id) {
        log.info("[SECTION] Deleting id='{}'", id);
        if (!sectionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Section", "id", id);
        }
        sectionRepository.deleteById(id);
    }

    // ── GET BY SEMESTER ───────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<SectionDTO> getSectionsBySemesterId(UUID semesterId) {
        findSemester(semesterId); // 404 guard
        return sectionRepository.findBySemesterId(semesterId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ── EXPORT EXCEL ──────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public ByteArrayInputStream exportToExcel() {
        log.info("[SECTION] Exporting all sections to Excel");
        return SectionExcelHelper.exportToExcel(getAllSections());
    }

    // ── TEMPLATE ──────────────────────────────────────────────────────────────
    @Override
    public ByteArrayInputStream downloadTemplate() {
        return SectionExcelHelper.downloadTemplate();
    }

    // ── IMPORT EXCEL ──────────────────────────────────────────────────────────
    @Override
    public List<SectionDTO> importFromExcel(MultipartFile file) {
        log.info("[SECTION] Importing sections from Excel");

        List<SectionDTO> dtos = SectionExcelHelper.importFromExcel(file);

        return dtos.stream().map(dto -> {

            Semester semester = findSemester(dto.getSemesterId());

            if (sectionRepository.existsByNameAndSemesterId(dto.getName(), dto.getSemesterId())) {
                throw new ExcelImportException(
                        "Section '" + dto.getName() + "' already exists in semester '"
                        + semester.getName() + "'");
            }

            Section section = sectionRepository.save(Section.builder()
                    .name(dto.getName())
                    .description(dto.getDescription())
                    .capacity(dto.getCapacity())
                    .semester(semester)
                    .build());

            log.info("[SECTION] Imported id='{}'", section.getId());
            return mapToDTO(section);

        }).collect(Collectors.toList());
    }

    // ── PRIVATE HELPERS ───────────────────────────────────────────────────────

    private Section findSection(UUID id) {
        return sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", id));
    }

    private Semester findSemester(UUID id) {
        return semesterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Semester", "id", id));
    }

    // ── MAPPER ────────────────────────────────────────────────────────────────
    private SectionDTO mapToDTO(Section s) {
        return SectionDTO.builder()
                .id(s.getId())
                .name(s.getName())
                .description(s.getDescription())
                .capacity(s.getCapacity())
                .semesterId(s.getSemester().getId())
                .semesterName(s.getSemester().getName())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
