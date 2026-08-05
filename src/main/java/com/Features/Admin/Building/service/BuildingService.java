package com.Features.Admin.Building.service;

import com.Features.Admin.Building.BuildingDTO.BuildingDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;

public interface BuildingService {
    BuildingDTO createBuildingDTO(BuildingDTO dto);
    BuildingDTO getBuildingDTOById(UUID id);
    List<BuildingDTO> getAllBuildingDTOs();
    BuildingDTO updateBuildingDTO(UUID id, BuildingDTO dto);
    void deleteBuildingDTOById(UUID id);

    // ── Excel ──────────────────────────────────────────────────────────────────
    ByteArrayInputStream exportToExcel();
    ByteArrayInputStream downloadTemplate();
    List<BuildingDTO> importFromExcel(MultipartFile file);
}
