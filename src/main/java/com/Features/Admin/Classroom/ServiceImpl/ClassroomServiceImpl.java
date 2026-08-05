package com.Features.Admin.Classroom.ServiceImpl;

import com.Features.Admin.Building.Repository.BuildingRepository;
import com.Features.Admin.Building.model.RegisterBuildingByAdmin;
import com.Features.Admin.Classroom.ClassroomService.ClassroomService;
import com.Features.Admin.Classroom.DTO.ClassroomDTO;
import com.Features.Admin.Classroom.excel.ClassroomExcelHelper;
import com.Features.Admin.Classroom.model.ClassType;
import com.Features.Admin.Classroom.model.Classroom;
import com.Features.Admin.Classroom.repository.ClassroomRepository;
import com.exception.ExcelImportException;
import com.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ClassroomServiceImpl implements ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final BuildingRepository buildingRepository;

    // ── CRUD ───────────────────────────────────────────────────────────────────
    @Override
    public ClassroomDTO createClassroom(ClassroomDTO dto) {
        log.info("[CLASSROOM] Creating '{}' type='{}' buildingId='{}'",
                dto.getName(), dto.getClassType(), dto.getBuildingId());

        RegisterBuildingByAdmin building = buildingRepository.findById(dto.getBuildingId())
                .orElseThrow(() -> new ResourceNotFoundException("Building", "id", dto.getBuildingId()));

        Classroom saved = classroomRepository.save(Classroom.builder()
                .name(dto.getName())
                .classType(dto.getClassType() != null ? dto.getClassType() : ClassType.LECTURE)
                .building(building)
                .build());
        log.info("[CLASSROOM] Created id='{}'", saved.getId());
        return mapToDTO(saved);
    }

    @Override
    public ClassroomDTO updateClassroom(UUID id, ClassroomDTO dto) {
        log.info("[CLASSROOM] Updating id='{}'", id);
        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", "id", id));
        RegisterBuildingByAdmin building = buildingRepository.findById(dto.getBuildingId())
                .orElseThrow(() -> new ResourceNotFoundException("Building", "id", dto.getBuildingId()));

        classroom.setName(dto.getName());
        classroom.setClassType(dto.getClassType() != null ? dto.getClassType() : classroom.getClassType());
        classroom.setBuilding(building);
        return mapToDTO(classroomRepository.save(classroom));
    }

    @Override
    public void deleteClassroom(UUID id) {
        log.info("[CLASSROOM] Deleting id='{}'", id);
        if (!classroomRepository.existsById(id)) throw new ResourceNotFoundException("Classroom", "id", id);
        classroomRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassroomDTO> findAllClassrooms() {
        return classroomRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ClassroomDTO findClassroomById(UUID id) {
        return mapToDTO(classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", "id", id)));
    }

    // ── Excel export ───────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public ByteArrayInputStream exportToExcel() {
        log.info("[CLASSROOM] Exporting all classrooms to Excel");
        return ClassroomExcelHelper.export(findAllClassrooms());
    }

    // ── Excel template ─────────────────────────────────────────────────────────
    @Override
    public ByteArrayInputStream downloadTemplate() {
        return ClassroomExcelHelper.template();
    }

    // ── Excel import ───────────────────────────────────────────────────────────
    @Override
    public List<ClassroomDTO> importFromExcel(MultipartFile file) {
        if (!ClassroomExcelHelper.hasExcelFormat(file)) {
            throw new ExcelImportException(
                    "Invalid file type. Please upload a .xlsx file.");
        }
        try {
            List<ClassroomDTO> parsed = ClassroomExcelHelper.parseExcel(file.getInputStream());
            log.info("[CLASSROOM] Importing {} row(s) from Excel", parsed.size());

            List<ClassroomDTO> saved = new ArrayList<>();
            for (int i = 0; i < parsed.size(); i++) {
                ClassroomDTO dto = parsed.get(i);
                RegisterBuildingByAdmin building = buildingRepository.findById(dto.getBuildingId())
                        .orElseThrow(() -> new ExcelImportException(
                                "Building not found for ID '" + dto.getBuildingId() + "'"));

                saved.add(mapToDTO(classroomRepository.save(Classroom.builder()
                        .name(dto.getName())
                        .classType(dto.getClassType())
                        .building(building)
                        .build())));
            }
            return saved;

        } catch (ExcelImportException e) {
            throw e;
        } catch (IOException e) {
            throw new ExcelImportException("Could not read file: " + e.getMessage());
        }
    }

    // ── Mapper ─────────────────────────────────────────────────────────────────
    public ClassroomDTO mapToDTO(Classroom c) {
        return ClassroomDTO.builder()
                .id(c.getId())
                .name(c.getName())
                .classType(c.getClassType())
                .buildingId(c.getBuilding().getId())
                .buildingName(c.getBuilding().getName())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
