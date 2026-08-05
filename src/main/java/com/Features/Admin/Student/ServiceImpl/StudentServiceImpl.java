package com.Features.Admin.Student.ServiceImpl;

import com.Features.Admin.Student.DTO.StudentDTO;
import com.Features.Admin.Student.excel.StudentExcelHelper;
import com.Features.Admin.Student.model.Student;
import com.Features.Admin.Student.model.StudentAccount;
import com.Features.Admin.Student.repository.StudentAccountRepository;
import com.Features.Admin.Student.repository.StudentRepository;
import com.Features.Admin.Student.service.StudentService;
import com.Features.Admin.Student.util.StudentPasswordGenerator;
import com.exception.DuplicateResourceException;
import com.exception.ExcelImportException;
import com.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
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
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final StudentAccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    // ── Create ─────────────────────────────────────────────────────────────────
    @Override
    public StudentDTO createStudent(StudentDTO dto) {
        log.info("[STUDENT] Creating student email='{}'", dto.getEmail());

        if (studentRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Student", "email", dto.getEmail());
        }

        Student student = studentRepository.save(buildStudentEntity(dto));
        log.info("[STUDENT] Created id='{}'", student.getId());

        // ── Password generated via StudentPasswordGenerator utility ───────────
        // File: com/Features/Admin/Student/util/StudentPasswordGenerator.java
        String rawPassword = StudentPasswordGenerator.generate(
                dto.getFirstName(), dto.getPhone(), student.getId());

        createAccount(student, rawPassword);
        log.info("[STUDENT] Account created for email='{}'", student.getEmail());

        return mapToDTO(student, rawPassword);
    }

    // ── Update ─────────────────────────────────────────────────────────────────
    @Override
    public StudentDTO updateStudent(UUID id, StudentDTO dto) {
        log.info("[STUDENT] Updating id='{}'", id);

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

        if (!student.getEmail().equalsIgnoreCase(dto.getEmail())
                && studentRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Student", "email", dto.getEmail());
        }

        student.setFirstName(dto.getFirstName());
        student.setLastName(dto.getLastName());
        student.setEmail(dto.getEmail());
        student.setPhone(dto.getPhone());
        student.setDateOfBirth(dto.getDateOfBirth());
        student.setGender(dto.getGender());
        student.setFatherName(dto.getFatherName());
        student.setFatherPhone(dto.getFatherPhone());
        student.setMotherName(dto.getMotherName());
        student.setMotherPhone(dto.getMotherPhone());
        student.setGuardianName(dto.getGuardianName());
        student.setGuardianPhone(dto.getGuardianPhone());

        // Keep login username in sync if email changed
        if (student.getAccount() != null) {
            student.getAccount().setUsername(dto.getEmail());
        }

        return mapToDTO(studentRepository.save(student), null);
    }

    // ── Find by ID ─────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public StudentDTO findStudentById(UUID id) {
        return mapToDTO(studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id)), null);
    }

    // ── Delete ─────────────────────────────────────────────────────────────────
    @Override
    public void deleteStudent(UUID id) {
        log.info("[STUDENT] Deleting id='{}'", id);
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Student", "id", id);
        }
        // StudentAccount deleted via CascadeType.ALL + orphanRemoval on Student
        studentRepository.deleteById(id);
    }

    // ── List all ───────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<StudentDTO> findAllStudents() {
        return studentRepository.findAll().stream()
                .map(s -> mapToDTO(s, null))
                .collect(Collectors.toList());
    }

    // ── Excel export ───────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public ByteArrayInputStream exportToExcel() {
        log.info("[STUDENT] Exporting all students to Excel");
        return StudentExcelHelper.export(findAllStudents());
    }

    // ── Excel template ─────────────────────────────────────────────────────────
    @Override
    public ByteArrayInputStream downloadTemplate() {
        return StudentExcelHelper.template();
    }

    // ── Excel import ───────────────────────────────────────────────────────────
    @Override
    public List<StudentDTO> importFromExcel(MultipartFile file) {
        if (!StudentExcelHelper.hasExcelFormat(file)) {
            throw new ExcelImportException("Invalid file type. Please upload a .xlsx file.");
        }

        try {
            List<StudentDTO> parsed = StudentExcelHelper.parseExcel(file.getInputStream());
            log.info("[STUDENT] Importing {} row(s) from Excel", parsed.size());

            // Fail fast — validate all emails before saving anything
            for (int i = 0; i < parsed.size(); i++) {
                String email = parsed.get(i).getEmail();
                if (studentRepository.existsByEmail(email)) {
                    throw new ExcelImportException(
                            "Row " + (i + 2) + ": Student with email '" + email + "' already exists.");
                }
            }

            List<StudentDTO> saved = new ArrayList<>();
            for (StudentDTO dto : parsed) {
                Student student = studentRepository.save(buildStudentEntity(dto));

                // ── Password generated via StudentPasswordGenerator utility ───
                // File: com/Features/Admin/Student/util/StudentPasswordGenerator.java
                String rawPassword = StudentPasswordGenerator.generate(
                        dto.getFirstName(), dto.getPhone(), student.getId());

                createAccount(student, rawPassword);
                saved.add(mapToDTO(student, rawPassword));
                log.info("[STUDENT] Imported + account created for email='{}'", student.getEmail());
            }

            return saved;

        } catch (ExcelImportException e) {
            throw e;
        } catch (IOException e) {
            throw new ExcelImportException("Could not read file: " + e.getMessage());
        }
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    /** Persists a BCrypt-hashed StudentAccount tied to the given student. */
    private void createAccount(Student student, String rawPassword) {
        accountRepository.save(StudentAccount.builder()
                .username(student.getEmail())
                .password(passwordEncoder.encode(rawPassword))
                .active(true)
                .student(student)
                .build());
    }

    private Student buildStudentEntity(StudentDTO dto) {
        return Student.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .dateOfBirth(dto.getDateOfBirth())
                .gender(dto.getGender())
                .fatherName(dto.getFatherName())
                .fatherPhone(dto.getFatherPhone())
                .motherName(dto.getMotherName())
                .motherPhone(dto.getMotherPhone())
                .guardianName(dto.getGuardianName())
                .guardianPhone(dto.getGuardianPhone())
                .build();
    }

    /**
     * rawPassword is non-null only right after account creation.
     * @JsonInclude(NON_NULL) on StudentDTO ensures it is omitted
     * from list / get / update responses automatically.
     */
    private StudentDTO mapToDTO(Student s, String rawPassword) {
        return StudentDTO.builder()
                .id(s.getId())
                .firstName(s.getFirstName())
                .lastName(s.getLastName())
                .email(s.getEmail())
                .phone(s.getPhone())
                .dateOfBirth(s.getDateOfBirth())
                .gender(s.getGender())
                .fatherName(s.getFatherName())
                .fatherPhone(s.getFatherPhone())
                .motherName(s.getMotherName())
                .motherPhone(s.getMotherPhone())
                .guardianName(s.getGuardianName())
                .guardianPhone(s.getGuardianPhone())
                .generatedPassword(rawPassword)
                .createdAt(s.getCreatedAt())
                .build();
    }
}
