package com.Features.Teacher.serviceImpl;

import com.Features.Admin.faculty.Faculty;
import com.Features.Admin.faculty.reposityory.FacultyRepository;
import com.Features.Teacher.dto.TeacherDTO;
import com.Features.Teacher.dto.TeacherLoginRequest;
import com.Features.Teacher.dto.TeacherLoginResponse;
import com.Features.Teacher.model.Teacher;
import com.Features.Teacher.model.TeacherAccount;
import com.Features.Teacher.repository.TeacherAccountRepository;
import com.Features.Teacher.repository.TeacherRepository;
import com.Features.Teacher.service.TeacherService;
import com.Features.Teacher.util.TeacherPasswordGenerator;
import com.exception.DuplicateResourceException;
import com.exception.ResourceNotFoundException;
import com.exception.UnauthorizedException;
import com.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TeacherServiceImpl implements TeacherService {

    private final TeacherRepository teacherRepository;
    private final TeacherAccountRepository accountRepository;
    private final FacultyRepository facultyRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    // ── Register teacher (Admin action) ───────────────────────────────────────
    @Override
    public TeacherDTO registerTeacher(TeacherDTO dto) {
        log.info("[TEACHER] Admin registering faculty id='{}' as teacher", dto.getFacultyId());

        Faculty faculty = facultyRepository.findById(dto.getFacultyId())
                .orElseThrow(() -> new ResourceNotFoundException("Faculty", "id", dto.getFacultyId()));

        // A faculty member can only be registered as teacher once
        if (teacherRepository.existsByFacultyId(dto.getFacultyId())) {
            throw new DuplicateResourceException("Teacher", "facultyId", dto.getFacultyId());
        }

        Teacher teacher = teacherRepository.save(Teacher.builder()
                .faculty(faculty)
                .notes(dto.getNotes())
                .active(true)
                .build());

        // ── Auto-generate password ─────────────────────────────────────────────
        String rawPassword = TeacherPasswordGenerator.generate(
                faculty.getFirstName(), faculty.getPhone(), teacher.getId());

        // Persist hashed account
        accountRepository.save(TeacherAccount.builder()
                .username(faculty.getEmail())
                .password(passwordEncoder.encode(rawPassword))
                .active(true)
                .teacher(teacher)
                .build());

        log.info("[TEACHER] Registered teacher id='{}' account username='{}'",
                teacher.getId(), faculty.getEmail());

        return mapToDTO(teacher, rawPassword);
    }

    // ── Get by ID ──────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public TeacherDTO getTeacherById(UUID id) {
        return mapToDTO(teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id)), null);
    }

    // ── Get all ────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<TeacherDTO> getAllTeachers() {
        return teacherRepository.findAll().stream()
                .map(t -> mapToDTO(t, null))
                .collect(Collectors.toList());
    }

    // ── Update ─────────────────────────────────────────────────────────────────
    @Override
    public TeacherDTO updateTeacher(UUID id, TeacherDTO dto) {
        log.info("[TEACHER] Updating id='{}'", id);

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id));

        teacher.setNotes(dto.getNotes());
        teacher.setActive(dto.isActive());

        // Sync account active flag
        if (teacher.getAccount() != null) {
            teacher.getAccount().setActive(dto.isActive());
        }

        return mapToDTO(teacherRepository.save(teacher), null);
    }

    // ── Delete ─────────────────────────────────────────────────────────────────
    @Override
    public void deleteTeacher(UUID id) {
        log.info("[TEACHER] Deleting id='{}'", id);
        if (!teacherRepository.existsById(id)) {
            throw new ResourceNotFoundException("Teacher", "id", id);
        }
        // TeacherAccount deleted via CascadeType.ALL + orphanRemoval on Teacher
        teacherRepository.deleteById(id);
    }

    // ── Login (Teacher auth) ───────────────────────────────────────────────────
    @Override
    public TeacherLoginResponse login(TeacherLoginRequest request) {
        log.info("[TEACHER LOGIN] Attempt for email='{}'", request.getEmail());

        TeacherAccount account = accountRepository.findByUsername(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("[TEACHER LOGIN] No account found for email='{}'", request.getEmail());
                    return new UnauthorizedException("Invalid email or password");
                });

        if (!account.isActive()) {
            log.warn("[TEACHER LOGIN] Account inactive for email='{}'", request.getEmail());
            throw new UnauthorizedException("Your teacher account is inactive. Contact the administrator.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (BadCredentialsException ex) {
            log.warn("[TEACHER LOGIN] Wrong password for email='{}'", request.getEmail());
            throw new UnauthorizedException("Invalid email or password");
        }

        UserDetails userDetails = User.builder()
                .username(account.getUsername())
                .password(account.getPassword())
                .authorities(new SimpleGrantedAuthority("ROLE_TEACHER"))
                .build();

        String token = jwtUtils.generateToken(userDetails, "ROLE_TEACHER");

        Faculty faculty = account.getTeacher().getFaculty();
        log.info("[TEACHER LOGIN] Success for email='{}'", request.getEmail());

        return TeacherLoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .teacherId(account.getTeacher().getId())
                .facultyId(faculty.getId())
                .firstName(faculty.getFirstName())
                .lastName(faculty.getLastName())
                .email(faculty.getEmail())
                .designation(faculty.getDesignation())
                .departmentName(faculty.getDepartment() != null
                        ? faculty.getDepartment().getName() : null)
                .role("ROLE_TEACHER")
                .build();
    }

    // ── My profile (Teacher action) ────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public TeacherDTO getMyProfile(String email) {
        Teacher teacher = teacherRepository.findByFacultyEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "email", email));
        return mapToDTO(teacher, null);
    }

    // ── Mapper ─────────────────────────────────────────────────────────────────
    private TeacherDTO mapToDTO(Teacher t, String rawPassword) {
        Faculty f = t.getFaculty();
        return TeacherDTO.builder()
                .id(t.getId())
                .facultyId(f.getId())
                .firstName(f.getFirstName())
                .middleName(f.getMiddleName())
                .lastName(f.getLastName())
                .email(f.getEmail())
                .phone(f.getPhone())
                .designation(f.getDesignation())
                .departmentName(f.getDepartment() != null ? f.getDepartment().getName() : null)
                .joiningDate(f.getJoiningDate())
                .notes(t.getNotes())
                .active(t.isActive())
                .generatedPassword(rawPassword)   // null → hidden by @JsonInclude(NON_NULL)
                .createdAt(t.getCreatedAt())
                .build();
    }
}
