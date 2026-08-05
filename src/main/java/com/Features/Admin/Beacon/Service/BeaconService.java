package com.Features.Admin.Beacon.Service;

import com.Features.Admin.Beacon.DTO.BeaconDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;

public interface BeaconService {
    BeaconDTO createBeacon(BeaconDTO dto);
    BeaconDTO updateBeacon(UUID id, BeaconDTO dto);
    BeaconDTO getBeaconById(UUID id);
    void deleteBeaconById(UUID id);
    List<BeaconDTO> getBeaconsByClassroomId(UUID classroomId);

    // ── Excel ──────────────────────────────────────────────────────────────────
    ByteArrayInputStream exportToExcel();
    ByteArrayInputStream downloadTemplate();
    List<BeaconDTO> importFromExcel(MultipartFile file);
}
