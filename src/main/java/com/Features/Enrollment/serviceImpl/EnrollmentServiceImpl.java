package com.Features.Enrollment.serviceImpl;

import com.Features.Admin.Section.Repository.SectionRepository;
import com.Features.Admin.Section.Section;
import com.Features.Admin.Semester.Repository.SemesterRepository;
import com.Features.Admin.Semester.Semester;
import com.Features.Admin.Student.model.Student;
import com.Features.Admin.Student.repository.StudentRepository;
import com.Features.Admin.Subject.Subject;
import com.Features.Admin.Subject.repository.SubjectRepository;
import com.Features.Enrollment.dto.BulkPromoteRequest;
import com.Features.Enrollment.dto.EnrollmentRequest;
import com.Features.Enrollment.dto.EnrollmentResponse;
import com.Features.Enrollment.dto.PromoteRequest;
import com.Features.Enrollment.model.Enrollment;
import com.Features.Enrollment.model.EnrollmentStatus;
import com.Features.Enrollment.repository.EnrollmentRepository;
import com.Features.Enrollment.service.EnrollmentService;
import com.common.EmailService;
import com.exception.AppException;
import com.exception.DuplicateResourceException;
import com.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final SemesterRepository semesterRepository;
    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;
    private final EmailService emailService;

    // ════════════════════════════════════════════════════════════════════
    // ENROLL
    // ════════════════════════════════════════════════════════════════════

    @Override
    public EnrollmentResponse enroll(EnrollmentRequest request) {
        log.info("[ENROLLMENT] Enrolling studentId='{}' semesterId='{}' sectionId='{}'",
                request.getStudentId(), request.getSemesterId(), request.getSectionId());

        Student student   = findStudent(request.getStudentId());
        Semester semester = findSemester(request.getSemesterId());
        Section section   = findSection(request.getSectionId());
        Set<Subject> subjects = resolveSubjects(request.getSubjectIds());

        // A student can only be enrolled ONCE per semester
        if (enrollmentRepository.existsByStudentIdAndSemesterId(
                request.getStudentId(), request.getSemesterId())) {
            throw new DuplicateResourceException(
                    "Enrollment", "student + semester",
                    student.getEmail() + " / " + semester.getName());
        }

        Enrollment saved = enrollmentRepository.save(Enrollment.builder()
                .student(student)
                .semester(semester)
                .section(section)
                .subjects(subjects)
                .status(EnrollmentStatus.ACTIVE)
                .remarks(request.getRemarks())
                .build());

        log.info("[ENROLLMENT] Enrolled id='{}'", saved.getId());

        // Send enrollment confirmation email asynchronously
        String fullName = student.getFirstName() + " " + student.getLastName();
        emailService.sendEnrollmentConfirmation(
                student.getEmail(),
                fullName,
                saved.getSemester().getName(),
                saved.getSection().getName(),
                saved.getSubjects().stream()
                        .map(s -> s.getSubjectName() + " (" + s.getSubjectCode() + ")")
                        .collect(java.util.stream.Collectors.joining(", ")));

        return mapToResponse(saved);
    }

    // ════════════════════════════════════════════════════════════════════
    // READ
    // ════════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public EnrollmentResponse getById(UUID id) {
        return mapToResponse(findEnrollment(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getHistoryByStudent(UUID studentId) {
        findStudent(studentId); // 404 guard
        return enrollmentRepository.findByStudentIdOrderByEnrolledAtDesc(studentId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getActiveBySection(UUID sectionId) {
        findSection(sectionId); // 404 guard
        return enrollmentRepository.findBySectionIdAndStatus(sectionId, EnrollmentStatus.ACTIVE)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getActiveBySemester(UUID semesterId) {
        findSemester(semesterId); // 404 guard
        return enrollmentRepository.findBySemesterIdAndStatus(semesterId, EnrollmentStatus.ACTIVE)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EnrollmentResponse getActiveEnrollmentByStudent(UUID studentId) {
        return mapToResponse(
                enrollmentRepository.findByStudentIdAndStatus(studentId, EnrollmentStatus.ACTIVE)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Active Enrollment", "studentId", studentId)));
    }

    // ════════════════════════════════════════════════════════════════════
    // PROMOTE — single
    // ════════════════════════════════════════════════════════════════════

    @Override
    public EnrollmentResponse promoteStudent(UUID enrollmentId, PromoteRequest request) {
        log.info("[ENROLLMENT] Promoting enrollmentId='{}'", enrollmentId);

        Enrollment current = findEnrollment(enrollmentId);

        if (current.getStatus() != EnrollmentStatus.ACTIVE) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Only ACTIVE enrollments can be promoted. Current status: " + current.getStatus());
        }

        Semester targetSemester = findSemester(request.getTargetSemesterId());
        Section  targetSection  = findSection(request.getTargetSectionId());
        Set<Subject> subjects   = resolveSubjects(request.getSubjectIds());

        // Prevent double-enrollment in target semester
        if (enrollmentRepository.existsByStudentIdAndSemesterId(
                current.getStudent().getId(), request.getTargetSemesterId())) {
            throw new DuplicateResourceException(
                    "Enrollment", "student + target semester",
                    current.getStudent().getEmail() + " / " + targetSemester.getName());
        }

        // Mark current enrollment as PROMOTED
        current.setStatus(EnrollmentStatus.PROMOTED);
        current.setPromotedAt(LocalDateTime.now());
        current.setRemarks(request.getRemarks() != null
                ? request.getRemarks() : "Promoted to " + targetSemester.getName());
        enrollmentRepository.save(current);

        // Create new ACTIVE enrollment for the target semester
        Enrollment promoted = enrollmentRepository.save(Enrollment.builder()
                .student(current.getStudent())
                .semester(targetSemester)
                .section(targetSection)
                .subjects(subjects)
                .status(EnrollmentStatus.ACTIVE)
                .remarks(request.getRemarks())
                .build());

        log.info("[ENROLLMENT] Promoted → new enrollmentId='{}'", promoted.getId());
        return mapToResponse(promoted);
    }

    // ════════════════════════════════════════════════════════════════════
    // PROMOTE — bulk
    // ════════════════════════════════════════════════════════════════════

    @Override
    public List<EnrollmentResponse> bulkPromote(BulkPromoteRequest request) {
        log.info("[ENROLLMENT] Bulk promote section='{}' semester='{}' → section='{}' semester='{}'",
                request.getSourceSectionId(), request.getSourceSemesterId(),
                request.getTargetSectionId(), request.getTargetSemesterId());

        Semester targetSemester = findSemester(request.getTargetSemesterId());
        Section  targetSection  = findSection(request.getTargetSectionId());
        Set<Subject> subjects   = resolveSubjects(request.getSubjectIds());

        List<Enrollment> source = enrollmentRepository.findBySectionIdAndSemesterIdAndStatus(
                request.getSourceSectionId(),
                request.getSourceSemesterId(),
                EnrollmentStatus.ACTIVE);

        if (source.isEmpty()) {
            throw new AppException(HttpStatus.NOT_FOUND,
                    "No ACTIVE enrollments found in the source section/semester.");
        }

        List<EnrollmentResponse> results = new ArrayList<>();
        int skipped = 0;

        for (Enrollment current : source) {
            // Skip students already enrolled in target semester
            if (enrollmentRepository.existsByStudentIdAndSemesterId(
                    current.getStudent().getId(), request.getTargetSemesterId())) {
                log.warn("[ENROLLMENT] Skipping studentId='{}' — already in target semester",
                        current.getStudent().getId());
                skipped++;
                continue;
            }

            // Mark old as PROMOTED
            current.setStatus(EnrollmentStatus.PROMOTED);
            current.setPromotedAt(LocalDateTime.now());
            current.setRemarks(request.getRemarks() != null
                    ? request.getRemarks() : "Bulk promoted to " + targetSemester.getName());
            enrollmentRepository.save(current);

            // Create new ACTIVE enrollment
            Enrollment promoted = enrollmentRepository.save(Enrollment.builder()
                    .student(current.getStudent())
                    .semester(targetSemester)
                    .section(targetSection)
                    .subjects(new HashSet<>(subjects))
                    .status(EnrollmentStatus.ACTIVE)
                    .remarks(request.getRemarks())
                    .build());

            results.add(mapToResponse(promoted));
        }

        log.info("[ENROLLMENT] Bulk promote done — promoted={} skipped={}", results.size(), skipped);
        return results;
    }

    // ════════════════════════════════════════════════════════════════════
    // DROP
    // ════════════════════════════════════════════════════════════════════

    @Override
    public EnrollmentResponse dropStudent(UUID enrollmentId, String reason) {
        log.info("[ENROLLMENT] Dropping enrollmentId='{}'", enrollmentId);

        Enrollment enrollment = findEnrollment(enrollmentId);

        if (enrollment.getStatus() == EnrollmentStatus.DROPPED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Enrollment is already DROPPED.");
        }

        enrollment.setStatus(EnrollmentStatus.DROPPED);
        enrollment.setRemarks(reason != null ? reason : "Dropped by admin");

        return mapToResponse(enrollmentRepository.save(enrollment));
    }

    // ════════════════════════════════════════════════════════════════════
    // UPDATE SUBJECTS
    // ════════════════════════════════════════════════════════════════════

    @Override
    public EnrollmentResponse updateSubjects(UUID enrollmentId, Set<UUID> subjectIds) {
        log.info("[ENROLLMENT] Updating subjects for enrollmentId='{}'", enrollmentId);

        Enrollment enrollment = findEnrollment(enrollmentId);

        if (enrollment.getStatus() != EnrollmentStatus.ACTIVE) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Subjects can only be updated for ACTIVE enrollments.");
        }

        enrollment.setSubjects(resolveSubjects(subjectIds));
        return mapToResponse(enrollmentRepository.save(enrollment));
    }

    // ════════════════════════════════════════════════════════════════════
    // MAPPERS
    // ════════════════════════════════════════════════════════════════════

    private EnrollmentResponse mapToResponse(Enrollment e) {
        Set<EnrollmentResponse.SubjectInfo> subjectInfos = e.getSubjects().stream()
                .map(s -> EnrollmentResponse.SubjectInfo.builder()
                        .id(s.getId())
                        .subjectName(s.getSubjectName())
                        .subjectCode(s.getSubjectCode())
                        .build())
                .collect(Collectors.toSet());

        String studentName = e.getStudent().getFirstName()
                + (e.getStudent().getLastName() != null ? " " + e.getStudent().getLastName() : "");

        return EnrollmentResponse.builder()
                .id(e.getId())
                .studentId(e.getStudent().getId())
                .studentName(studentName)
                .studentEmail(e.getStudent().getEmail())
                .semesterId(e.getSemester().getId())
                .semesterName(e.getSemester().getName())
                .sectionId(e.getSection().getId())
                .sectionName(e.getSection().getName())
                .subjects(subjectInfos)
                .status(e.getStatus())
                .remarks(e.getRemarks())
                .promotedAt(e.getPromotedAt())
                .enrolledAt(e.getEnrolledAt())
                .build();
    }

    // ════════════════════════════════════════════════════════════════════
    // LOOKUP HELPERS
    // ════════════════════════════════════════════════════════════════════

    private Enrollment findEnrollment(UUID id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", id));
    }

    private Student findStudent(UUID id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));
    }

    private Semester findSemester(UUID id) {
        return semesterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Semester", "id", id));
    }

    private Section findSection(UUID id) {
        return sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", id));
    }

    private Set<Subject> resolveSubjects(Set<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "At least one subject ID is required.");
        }
        Set<Subject> subjects = new HashSet<>(subjectRepository.findAllById(ids));
        if (subjects.size() != ids.size()) {
            throw new AppException(HttpStatus.NOT_FOUND,
                    "One or more subject IDs were not found. Provided: " + ids.size()
                    + ", Found: " + subjects.size());
        }
        return subjects;
    }
}
