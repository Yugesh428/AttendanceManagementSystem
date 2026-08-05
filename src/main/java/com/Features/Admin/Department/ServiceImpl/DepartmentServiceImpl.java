package com.Features.Admin.Department.ServiceImpl;

import com.Features.Admin.Department.DTO.DepartmentDTO;
import com.Features.Admin.Department.excel.DepartmentExcelHelper;
import com.Features.Admin.Department.model.Department;
import com.Features.Admin.Department.repository.DepartmentRepository;
import com.Features.Admin.Department.service.DepartmentService;
import com.exception.DuplicateResourceException;
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
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository repository;

    // ── Create ─────────────────────────────────────────────────────────────────
    @Override
    public DepartmentDTO createDepartmentDTO(DepartmentDTO dto) {
        log.info("[DEPARTMENT] Creating name='{}' code='{}'", dto.getName(), dto.getCode());

        if (repository.existsByName(dto.getName())) {
            throw new DuplicateResourceException("Department", "name", dto.getName());
        }
        if (repository.existsByCode(dto.getCode())) {
            throw new DuplicateResourceException("Department", "code", dto.getCode());
        }

        Department saved = repository.save(Department.builder()
                .name(dto.getName().trim())
                .code(dto.getCode().trim().toUpperCase())
                .description(dto.getDescription())
                .build());

        log.info("[DEPARTMENT] Created id='{}'", saved.getId());
        return mapToDTO(saved);
    }

    // ── Get by ID ──────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public DepartmentDTO getDepartmentDTOById(UUID id) {
        return mapToDTO(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id)));
    }

    // ── Get all ────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<DepartmentDTO> getAllDepartmentDTOs() {
        return repository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ── Update ─────────────────────────────────────────────────────────────────
    @Override
    public DepartmentDTO updateDepartmentDTO(UUID id, DepartmentDTO dto) {
        log.info("[DEPARTMENT] Updating id='{}'", id);

        Department entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        // Check name uniqueness — ignore current record
        if (repository.existsByNameAndIdNot(dto.getName(), id)) {
            throw new DuplicateResourceException("Department", "name", dto.getName());
        }
        // Check code uniqueness — ignore current record
        if (repository.existsByCodeAndIdNot(dto.getCode().toUpperCase(), id)) {
            throw new DuplicateResourceException("Department", "code", dto.getCode());
        }

        entity.setName(dto.getName().trim());
        entity.setCode(dto.getCode().trim().toUpperCase());
        entity.setDescription(dto.getDescription());

        return mapToDTO(repository.save(entity));
    }

    // ── Delete ─────────────────────────────────────────────────────────────────
    @Override
    public void deleteDepartmentDTOById(UUID id) {
        log.info("[DEPARTMENT] Deleting id='{}'", id);
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Department", "id", id);
        }
        repository.deleteById(id);
    }

    // ── Excel export ───────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public ByteArrayInputStream exportToExcel() {
        log.info("[DEPARTMENT] Exporting all departments to Excel");
        return DepartmentExcelHelper.export(getAllDepartmentDTOs());
    }

    // ── Excel template ─────────────────────────────────────────────────────────
    @Override
    public ByteArrayInputStream downloadTemplate() {
        return DepartmentExcelHelper.template();
    }

    // ── Excel import ───────────────────────────────────────────────────────────
    @Override
    public List<DepartmentDTO> importFromExcel(MultipartFile file) {
        if (!DepartmentExcelHelper.hasExcelFormat(file)) {
            throw new ExcelImportException("Invalid file type. Please upload a .xlsx file.");
        }

        try {
            List<DepartmentDTO> parsed = DepartmentExcelHelper.parseExcel(file.getInputStream());
            log.info("[DEPARTMENT] Importing {} row(s) from Excel", parsed.size());

            // Fail fast — validate all names and codes before saving anything
            for (int i = 0; i < parsed.size(); i++) {
                DepartmentDTO dto = parsed.get(i);
                int rowNum = i + 2; // +2 because row 1 is header, data starts at row 2

                if (repository.existsByName(dto.getName())) {
                    throw new ExcelImportException(
                            "Row " + rowNum + ": Department with name '" + dto.getName() + "' already exists.");
                }
                if (repository.existsByCode(dto.getCode().toUpperCase())) {
                    throw new ExcelImportException(
                            "Row " + rowNum + ": Department with code '" + dto.getCode() + "' already exists.");
                }
            }

            List<DepartmentDTO> saved = new ArrayList<>();
            for (DepartmentDTO dto : parsed) {
                Department dept = repository.save(Department.builder()
                        .name(dto.getName().trim())
                        .code(dto.getCode().trim().toUpperCase())
                        .description(dto.getDescription())
                        .build());
                saved.add(mapToDTO(dept));
                log.info("[DEPARTMENT] Imported name='{}' code='{}'", dept.getName(), dept.getCode());
            }

            return saved;

        } catch (ExcelImportException e) {
            throw e;
        } catch (IOException e) {
            throw new ExcelImportException("Could not read file: " + e.getMessage());
        }
    }

    // ── Mapper ─────────────────────────────────────────────────────────────────
    private DepartmentDTO mapToDTO(Department e) {
        return DepartmentDTO.builder()
                .id(e.getId())
                .name(e.getName())
                .code(e.getCode())
                .description(e.getDescription())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
