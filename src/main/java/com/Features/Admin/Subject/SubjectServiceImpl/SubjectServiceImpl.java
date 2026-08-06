package com.Features.Admin.Subject.SubjectServiceImpl;

import com.Features.Admin.CourseCategory.model.CourseCategory;
import com.Features.Admin.CourseCategory.repository.CourseCategoryRepository;
import com.Features.Admin.Subject.DTO.SubjectResponseDTO;
import com.Features.Admin.Subject.Subject;
import com.Features.Admin.Subject.Service.SubjectService;
import com.Features.Admin.Subject.repository.SubjectRepository;
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
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final CourseCategoryRepository courseCategoryRepository;

    // ── Create ─────────────────────────────────────────────────────────────────
    @Override
    public SubjectResponseDTO createSubject(SubjectResponseDTO dto) {
        log.info("[SUBJECT] Creating name='{}' code='{}'", dto.getSubjectName(), dto.getSubjectCode());

        // Bug fix 3: DuplicateResourceException (409) instead of RuntimeException (400)
        if (subjectRepository.existsBySubjectCode(dto.getSubjectCode())) {
            throw new DuplicateResourceException("Subject", "subjectCode", dto.getSubjectCode());
        }

        CourseCategory category = courseCategoryRepository.findById(dto.getCourseCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CourseCategory", "id", dto.getCourseCategoryId()));

        Subject saved = subjectRepository.save(Subject.builder()
                .subjectName(dto.getSubjectName())
                .subjectCode(dto.getSubjectCode().toUpperCase())
                .courseCategory(category)
                .build());

        log.info("[SUBJECT] Created id='{}'", saved.getId());
        return mapToDTO(saved);
    }

    // ── Get all ────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)   // Bug fix 4: was missing readOnly
    public List<SubjectResponseDTO> getAllSubjects() {
        return subjectRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ── Get by ID ──────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)   // Bug fix 4: was missing readOnly
    public SubjectResponseDTO getSubjectById(UUID id) {
        return mapToDTO(subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", id)));
    }

    // ── Update ─────────────────────────────────────────────────────────────────
    @Override
    public SubjectResponseDTO updateSubject(UUID id, SubjectResponseDTO dto) {
        log.info("[SUBJECT] Updating id='{}'", id);

        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", id));

        // Bug fix 3: DuplicateResourceException (409) instead of RuntimeException (400)
        if (!subject.getSubjectCode().equalsIgnoreCase(dto.getSubjectCode())
                && subjectRepository.existsBySubjectCode(dto.getSubjectCode())) {
            throw new DuplicateResourceException("Subject", "subjectCode", dto.getSubjectCode());
        }

        CourseCategory category = courseCategoryRepository.findById(dto.getCourseCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CourseCategory", "id", dto.getCourseCategoryId()));

        subject.setSubjectName(dto.getSubjectName());
        subject.setSubjectCode(dto.getSubjectCode().toUpperCase());
        subject.setCourseCategory(category);

        return mapToDTO(subjectRepository.save(subject));
    }

    // ── Delete ─────────────────────────────────────────────────────────────────
    @Override
    public void deleteSubject(UUID id) {
        log.info("[SUBJECT] Deleting id='{}'", id);
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", id));
        subjectRepository.delete(subject);
    }

    // ── Mapper ─────────────────────────────────────────────────────────────────
    private SubjectResponseDTO mapToDTO(Subject s) {
        return SubjectResponseDTO.builder()
                .id(s.getId())
                .subjectName(s.getSubjectName())
                .subjectCode(s.getSubjectCode())
                .courseCategoryId(s.getCourseCategory().getId())
                // Bug fix 5: CourseCategory field is "courseName", NOT "getName()" — would throw NoSuchMethodError
                .courseCategoryName(s.getCourseCategory().getCourseName())
                .build();
    }
}
