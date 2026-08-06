package com.Features.Admin.faculty.ServiceImpl;

import com.Features.Admin.Department.model.Department;
import com.Features.Admin.Department.repository.DepartmentRepository;
import com.Features.Admin.faculty.DTO.FacultyDTO;
import com.Features.Admin.faculty.Faculty;
import com.Features.Admin.faculty.FacultyStatus;
import com.Features.Admin.faculty.Service.FacultyService;
import com.Features.Admin.faculty.reposityory.FacultyRepository;
import com.exception.DuplicateResourceException;
import com.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FacultyServiceImpl implements FacultyService {

    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;

    // ── Create ─────────────────────────────────────────────────────────────────
    @Override
    public FacultyDTO createFaculty(FacultyDTO dto) {
        log.info("[FACULTY] Creating '{}' '{}'", dto.getFirstName(), dto.getLastName());

        // Bug fix 3: use DuplicateResourceException (409) instead of RuntimeException (400)
        if (facultyRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Faculty", "email", dto.getEmail());
        }

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", dto.getDepartmentId()));

        Faculty saved = facultyRepository.save(Faculty.builder()
                .firstName(dto.getFirstName())
                .middleName(dto.getMiddleName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .description(dto.getDescription())
                .designation(dto.getDesignation())
                .joiningDate(dto.getJoiningDate())
                .status(FacultyStatus.valueOf(dto.getStatus().toUpperCase()))
                .createdBy(dto.getCreatedBy())
                .department(department)
                .build());

        log.info("[FACULTY] Created id='{}'", saved.getId());
        return mapToDTO(saved);
    }

    // ── Get by ID ──────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)   // ← correct spelling
    public FacultyDTO getFacultyById(UUID id) {
        return mapToDTO(facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty", "id", id)));
    }

    // ── Get all ────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)   // Bug fix 1: was raedOnly = true (typo, silently ignored)
    public List<FacultyDTO> getAllFaculty() {
        return facultyRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ── Update ─────────────────────────────────────────────────────────────────
    @Override
    public FacultyDTO updateFaculty(UUID id, FacultyDTO dto) {
        log.info("[FACULTY] Updating id='{}'", id);

        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty", "id", id));

        // If email is changing, make sure the new one isn't taken
        if (!faculty.getEmail().equalsIgnoreCase(dto.getEmail())
                && facultyRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Faculty", "email", dto.getEmail());
        }

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", dto.getDepartmentId()));

        faculty.setFirstName(dto.getFirstName());
        faculty.setMiddleName(dto.getMiddleName());
        faculty.setLastName(dto.getLastName());
        faculty.setEmail(dto.getEmail());
        faculty.setPhone(dto.getPhone());
        faculty.setAddress(dto.getAddress());
        faculty.setDescription(dto.getDescription());
        faculty.setDesignation(dto.getDesignation());
        faculty.setJoiningDate(dto.getJoiningDate());
        faculty.setStatus(FacultyStatus.valueOf(dto.getStatus().toUpperCase()));
        faculty.setDepartment(department);

        return mapToDTO(facultyRepository.save(faculty));
    }

    // ── Delete ─────────────────────────────────────────────────────────────────
    @Override
    public void deleteFaculty(UUID id) {
        log.info("[FACULTY] Deleting id='{}'", id);

        // Bug fix 2: was existsById (missing !) — threw "not found" when record EXISTS
        if (!facultyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Faculty", "id", id);
        }
        facultyRepository.deleteById(id);
    }

    // ── Mapper ─────────────────────────────────────────────────────────────────
    public FacultyDTO mapToDTO(Faculty f) {
        return FacultyDTO.builder()
                .id(f.getId())
                .firstName(f.getFirstName())
                .middleName(f.getMiddleName())
                .lastName(f.getLastName())
                .email(f.getEmail())
                .phone(f.getPhone())
                .address(f.getAddress())
                .description(f.getDescription())
                .designation(f.getDesignation())
                .joiningDate(f.getJoiningDate())
                .status(f.getStatus().name())
                .departmentId(f.getDepartment().getId())
                .createdBy(f.getCreatedBy())
                .build();
    }
}
