package com.Features.Admin.Beacon.ServiceImpl;

import com.Features.Admin.Beacon.Beacon;
import com.Features.Admin.Beacon.DTO.BeaconDTO;
import com.Features.Admin.Beacon.Repository.BeaconRepository;
import com.Features.Admin.Beacon.Service.BeaconService;
import com.Features.Admin.Beacon.excel.BeaconExcelHelper;
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
public class BeaconServiceImpl implements BeaconService {

    private final BeaconRepository beaconRepository;
    private final ClassroomRepository classroomRepository;

    // ── CRUD ───────────────────────────────────────────────────────────────────
    @Override
    public BeaconDTO createBeacon(BeaconDTO dto) {
        log.info("[BEACON] Creating uuid='{}' classroomId='{}'", dto.getUuid(), dto.getClassroomId());
        Classroom classroom = classroomRepository.findById(dto.getClassroomId())
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", "id", dto.getClassroomId()));
        Beacon saved = beaconRepository.save(Beacon.builder()
                .uuid(dto.getUuid()).major(dto.getMajor()).minor(dto.getMinor()).classroom(classroom).build());
        log.info("[BEACON] Created id='{}'", saved.getId());
        return mapToDTO(saved);
    }

    @Override
    public BeaconDTO updateBeacon(UUID id, BeaconDTO dto) {
        log.info("[BEACON] Updating id='{}'", id);
        Beacon beacon = beaconRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Beacon", "id", id));
        Classroom classroom = classroomRepository.findById(dto.getClassroomId())
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", "id", dto.getClassroomId()));
        beacon.setUuid(dto.getUuid());
        beacon.setMajor(dto.getMajor());
        beacon.setMinor(dto.getMinor());
        beacon.setClassroom(classroom);
        return mapToDTO(beaconRepository.save(beacon));
    }

    @Override
    @Transactional(readOnly = true)
    public BeaconDTO getBeaconById(UUID id) {
        return mapToDTO(beaconRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Beacon", "id", id)));
    }

    @Override
    public void deleteBeaconById(UUID id) {
        log.info("[BEACON] Deleting id='{}'", id);
        if (!beaconRepository.existsById(id)) throw new ResourceNotFoundException("Beacon", "id", id);
        beaconRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BeaconDTO> getBeaconsByClassroomId(UUID classroomId) {
        if (!classroomRepository.existsById(classroomId)) {
            throw new ResourceNotFoundException("Classroom", "id", classroomId);
        }
        return beaconRepository.findByClassroomId(classroomId)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // ── Excel export ───────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public ByteArrayInputStream exportToExcel() {
        log.info("[BEACON] Exporting all beacons to Excel");
        List<BeaconDTO> all = beaconRepository.findAll()
                .stream().map(this::mapToDTO).collect(Collectors.toList());
        return BeaconExcelHelper.export(all);
    }

    // ── Excel template ─────────────────────────────────────────────────────────
    @Override
    public ByteArrayInputStream downloadTemplate() {
        return BeaconExcelHelper.template();
    }

    // ── Excel import ───────────────────────────────────────────────────────────
    @Override
    public List<BeaconDTO> importFromExcel(MultipartFile file) {
        if (!BeaconExcelHelper.hasExcelFormat(file)) {
            throw new ExcelImportException("Invalid file type. Please upload a .xlsx file.");
        }
        try {
            List<BeaconDTO> parsed = BeaconExcelHelper.parseExcel(file.getInputStream());
            log.info("[BEACON] Importing {} row(s) from Excel", parsed.size());

            List<BeaconDTO> saved = new ArrayList<>();
            for (BeaconDTO dto : parsed) {
                Classroom classroom = classroomRepository.findById(dto.getClassroomId())
                        .orElseThrow(() -> new ExcelImportException(
                                "Classroom not found for ID '" + dto.getClassroomId() + "'"));
                saved.add(mapToDTO(beaconRepository.save(Beacon.builder()
                        .uuid(dto.getUuid())
                        .major(dto.getMajor())
                        .minor(dto.getMinor())
                        .classroom(classroom)
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
    private BeaconDTO mapToDTO(Beacon b) {
        return BeaconDTO.builder()
                .id(b.getId())
                .uuid(b.getUuid())
                .major(b.getMajor())
                .minor(b.getMinor())
                .classroomId(b.getClassroom().getId())
                .classroomName(b.getClassroom().getName())
                .createdAt(b.getCreatedAt())
                .build();
    }
}
