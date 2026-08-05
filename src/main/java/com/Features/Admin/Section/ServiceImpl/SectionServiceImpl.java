package com.Features.Admin.Section.ServiceImpl;

import com.Features.Admin.Section.DTO.SectionDTO;
import com.Features.Admin.Section.excel.SectionExcelHelper;
import com.Features.Admin.Section.Section;
import com.Features.Admin.Section.Repository.SectionRepository;
import com.Features.Admin.Section.Service.SectionService;
import com.Features.Admin.Semester.Semester;
import com.Features.Admin.Semester.Repository.SemesterRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SectionServiceImpl implements SectionService {

    private final SectionRepository sectionRepository;
    private final SemesterRepository semesterRepository;

    // ── CREATE ────────────────────────────────────────────────────────────────
    @Override
    public SectionDTO createSection(SectionDTO dto) {

        Semester semester = semesterRepository.findById(dto.getSemesterId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Semester not found with ID: " + dto.getSemesterId()));

        // Duplicate check
        if (sectionRepository.existsByNameAndSemesterId(dto.getName(), dto.getSemesterId())) {
            throw new RuntimeException("Section already exists in this semester");
        }

        Section section = Section.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .capacity(dto.getCapacity())
                .createdAt(LocalDateTime.now())
                .semester(semester)
                .build();

        return mapToDTO(sectionRepository.save(section));
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────
    @Override
    public SectionDTO updateSection(UUID id, SectionDTO dto) {

        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Section not found"));

        Semester semester = semesterRepository.findById(dto.getSemesterId())
                .orElseThrow(() -> new EntityNotFoundException("Semester not found"));

        section.setName(dto.getName());
        section.setDescription(dto.getDescription());
        section.setCapacity(dto.getCapacity());
        section.setSemester(semester);

        return mapToDTO(sectionRepository.save(section));
    }

    // ── GET BY ID ─────────────────────────────────────────────────────────────
    @Override
    public SectionDTO getSectionById(UUID id) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Section not found"));

        return mapToDTO(section);
    }

    // ── GET ALL ───────────────────────────────────────────────────────────────
    @Override
    public List<SectionDTO> getAllSections() {
        return sectionRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
    @Override
    public void deleteSection(UUID id) {
        if (!sectionRepository.existsById(id)) {
            throw new EntityNotFoundException("Section not found");
        }
        sectionRepository.deleteById(id);
    }

    // ── GET BY SEMESTER ───────────────────────────────────────────────────────
    @Override
    public List<SectionDTO> getSectionsBySemesterId(UUID semesterId) {
        return sectionRepository.findBySemesterId(semesterId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ── EXPORT EXCEL ──────────────────────────────────────────────────────────
    @Override
    public ByteArrayInputStream exportToExcel() {
        List<SectionDTO> sections = getAllSections();
        return SectionExcelHelper.exportToExcel(sections);
    }

    // ── TEMPLATE ──────────────────────────────────────────────────────────────
    @Override
    public ByteArrayInputStream downloadTemplate() {
        return SectionExcelHelper.downloadTemplate();
    }

    // ── IMPORT EXCEL ──────────────────────────────────────────────────────────
    @Override
    public List<SectionDTO> importFromExcel(MultipartFile file) {

        List<SectionDTO> dtos = SectionExcelHelper.importFromExcel(file);

        return dtos.stream().map(dto -> {

            Semester semester = semesterRepository.findById(dto.getSemesterId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Semester not found with ID: " + dto.getSemesterId()));

            // Duplicate check
            if (sectionRepository.existsByNameAndSemesterId(dto.getName(), dto.getSemesterId())) {
                throw new RuntimeException("Duplicate section in row for name: " + dto.getName());
            }

            Section section = Section.builder()
                    .name(dto.getName())
                    .description(dto.getDescription())
                    .capacity(dto.getCapacity())
                    .createdAt(LocalDateTime.now())
                    .semester(semester)
                    .build();

            return mapToDTO(sectionRepository.save(section));

        }).collect(Collectors.toList());
    }

    // ── MAPPER ────────────────────────────────────────────────────────────────
    private SectionDTO mapToDTO(Section section) {
        return SectionDTO.builder()
                .id(section.getId())
                .name(section.getName())
                .description(section.getDescription())
                .capacity(section.getCapacity())
                .semesterId(section.getSemester().getId())
                .semesterName(section.getSemester().getName())
                .createdAt(section.getCreatedAt())
                .build();
    }
}