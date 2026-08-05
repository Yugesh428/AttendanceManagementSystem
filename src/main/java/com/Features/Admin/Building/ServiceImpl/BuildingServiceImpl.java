package com.Features.Admin.Building.ServiceImpl;

import com.Features.Admin.Building.BuildingDTO.BuildingDTO;
import com.Features.Admin.Building.Repository.BuildingRepository;
import com.Features.Admin.Building.excel.BuildingExcelHelper;
import com.Features.Admin.Building.model.RegisterBuildingByAdmin;
import com.Features.Admin.Building.service.BuildingService;
import com.exception.ExcelImportException;
import com.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BuildingServiceImpl implements BuildingService {

    private final BuildingRepository repository;

    // ── CRUD ───────────────────────────────────────────────────────────────────
    @Override
    public BuildingDTO createBuildingDTO(BuildingDTO dto) {
        log.info("[BUILDING] Creating name='{}'", dto.getName());
        RegisterBuildingByAdmin saved = repository.save(
                RegisterBuildingByAdmin.builder()
                        .name(dto.getName())
                        .location(dto.getLocation())
                        .build());
        log.info("[BUILDING] Created id='{}'", saved.getId());
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BuildingDTO getBuildingDTOById(UUID id) {
        return mapToDTO(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Building", "id", id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BuildingDTO> getAllBuildingDTOs() {
        return repository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public BuildingDTO updateBuildingDTO(UUID id, BuildingDTO dto) {
        log.info("[BUILDING] Updating id='{}'", id);
        RegisterBuildingByAdmin entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Building", "id", id));
        entity.setName(dto.getName());
        entity.setLocation(dto.getLocation());
        return mapToDTO(repository.save(entity));
    }

    @Override
    public void deleteBuildingDTOById(UUID id) {
        log.info("[BUILDING] Deleting id='{}'", id);
        if (!repository.existsById(id)) throw new ResourceNotFoundException("Building", "id", id);
        repository.deleteById(id);
    }

    // ── Excel export ───────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public ByteArrayInputStream exportToExcel() {
        log.info("[BUILDING] Exporting all buildings to Excel");
        return BuildingExcelHelper.export(getAllBuildingDTOs());
    }

    // ── Excel template download ────────────────────────────────────────────────
    @Override
    public ByteArrayInputStream downloadTemplate() {
        return BuildingExcelHelper.template();
    }

    // ── Excel import ───────────────────────────────────────────────────────────
    @Override
    public List<BuildingDTO> importFromExcel(MultipartFile file) {
        if (!BuildingExcelHelper.hasExcelFormat(file)) {
            throw new ExcelImportException(
                    "Invalid file type. Please upload a .xlsx file (content-type: "
                    + "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet).");
        }
        try {
            List<BuildingDTO> parsed = BuildingExcelHelper.parseExcel(file.getInputStream());
            log.info("[BUILDING] Importing {} row(s) from Excel", parsed.size());

            // Save all rows and return saved DTOs
            return parsed.stream()
                    .map(dto -> repository.save(
                            RegisterBuildingByAdmin.builder()
                                    .name(dto.getName())
                                    .location(dto.getLocation())
                                    .build()))
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());

        } catch (ExcelImportException e) {
            throw e;
        } catch (IOException e) {
            throw new ExcelImportException("Could not read file: " + e.getMessage());
        }
    }

    // ── Mapper ─────────────────────────────────────────────────────────────────
    private BuildingDTO mapToDTO(RegisterBuildingByAdmin e) {
        return BuildingDTO.builder()
                .id(e.getId())
                .name(e.getName())
                .location(e.getLocation())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
