package com.Features.ModuleLeader.serviceImpl;

import com.Features.Admin.Subject.Subject;
import com.Features.Admin.Subject.repository.SubjectRepository;
import com.Features.Attendance.dto.AttendanceRecordResponse;
import com.Features.Attendance.dto.AttendanceReportResponse;
import com.Features.Attendance.dto.SlotHistoryResponse;
import com.Features.Attendance.model.AttendanceStatus;
import com.Features.Attendance.repository.AttendanceRepository;
import com.Features.Enrollment.model.EnrollmentStatus;
import com.Features.Enrollment.repository.EnrollmentRepository;
import com.Features.ModuleLeader.dto.CreateModuleLeaderRequest;
import com.Features.ModuleLeader.dto.ModuleLeaderLoginRequest;
import com.Features.ModuleLeader.dto.ModuleLeaderLoginResponse;
import com.Features.ModuleLeader.dto.ModuleLeaderResponse;
import com.Features.ModuleLeader.dto.StudentAttendanceSummaryDTO;
import com.Features.ModuleLeader.dto.TeacherSummaryDTO;
import com.Features.ModuleLeader.model.ModuleLeader;
import com.Features.ModuleLeader.model.ModuleLeaderAccount;
import com.Features.ModuleLeader.repository.ModuleLeaderAccountRepository;
import com.Features.ModuleLeader.repository.ModuleLeaderRepository;
import com.Features.ModuleLeader.service.ModuleLeaderService;
import com.Features.Teacher.model.Teacher;
import com.Features.Timetable.dto.TimetableSlotDTO;
import com.Features.Timetable.model.TimetableSlot;
import com.Features.Timetable.repository.TimetableSlotRepository;
import com.common.EmailService;
import com.exception.AppException;
import com.exception.DuplicateResourceException;
import com.exception.ResourceNotFoundException;
import com.exception.UnauthorizedException;
import com.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ModuleLeaderServiceImpl implements ModuleLeaderService {

    private final ModuleLeaderRepository        moduleLeaderRepository;
    private final ModuleLeaderAccountRepository accountRepository;
    private final SubjectRepository             subjectRepository;
    private final TimetableSlotRepository       slotRepository;
    private final AttendanceRepository          attendanceRepository;
    private final EnrollmentRepository          enrollmentRepository;
    private final AuthenticationManager         authenticationManager;
    private final JwtUtils                      jwtUtils;
    private final PasswordEncoder               passwordEncoder;
    private final EmailService                  emailService;

    // ════════════════════════════════════════════════════════════════════
    // ADMIN — CRUD
    // ════════════════════════════════════════════════════════════════════

    @Override
    public ModuleLeaderResponse create(CreateModuleLeaderRequest req) {
        log.info("[MODULE LEADER] Creating email='{}' subjectId='{}'",
                req.getEmail(), req.getSubjectId());

        if (moduleLeaderRepository.existsByEmail(req.getEmail())) {
            throw new DuplicateResourceException("ModuleLeader", "email", req.getEmail());
        }

        Subject subject = subjectRepository.findById(req.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", req.getSubjectId()));

        ModuleLeader saved = moduleLeaderRepository.save(ModuleLeader.builder()
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .subject(subject)
                .active(true)
                .build());

        // Generate password: first4(firstName) + "@" + last4(phone or uuid)
        String rawPassword = generatePassword(req.getFirstName(), req.getPhone(), saved.getId());

        accountRepository.save(ModuleLeaderAccount.builder()
                .username(saved.getEmail())
                .password(passwordEncoder.encode(rawPassword))
                .active(true)
                .moduleLeader(saved)
                .build());

        // Email credentials with module-leader-specific message
        String fullName = saved.getFirstName()
                + (saved.getLastName() != null ? " " + saved.getLastName() : "");
        emailService.sendModuleLeaderCredentials(
                saved.getEmail(), fullName, saved.getEmail(),
                rawPassword, subject.getSubjectName());

        log.info("[MODULE LEADER] Created id='{}'", saved.getId());
        return mapToResponse(saved, rawPassword);
    }

    @Override
    @Transactional(readOnly = true)
    public ModuleLeaderResponse getById(UUID id) {
        return mapToResponse(findById(id), null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModuleLeaderResponse> getAll() {
        return moduleLeaderRepository.findAll()
                .stream().map(ml -> mapToResponse(ml, null)).collect(Collectors.toList());
    }

    @Override
    public ModuleLeaderResponse update(UUID id, CreateModuleLeaderRequest req) {
        log.info("[MODULE LEADER] Updating id='{}'", id);
        ModuleLeader ml = findById(id);

        if (!ml.getEmail().equalsIgnoreCase(req.getEmail())
                && moduleLeaderRepository.existsByEmail(req.getEmail())) {
            throw new DuplicateResourceException("ModuleLeader", "email", req.getEmail());
        }

        Subject subject = subjectRepository.findById(req.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", req.getSubjectId()));

        ml.setFirstName(req.getFirstName());
        ml.setLastName(req.getLastName());
        ml.setEmail(req.getEmail());
        ml.setPhone(req.getPhone());
        ml.setSubject(subject);

        if (ml.getAccount() != null) {
            ml.getAccount().setUsername(req.getEmail());
        }

        return mapToResponse(moduleLeaderRepository.save(ml), null);
    }

    @Override
    public void delete(UUID id) {
        log.info("[MODULE LEADER] Deleting id='{}'", id);
        if (!moduleLeaderRepository.existsById(id)) {
            throw new ResourceNotFoundException("ModuleLeader", "id", id);
        }
        moduleLeaderRepository.deleteById(id);
    }

    // ════════════════════════════════════════════════════════════════════
    // AUTH — LOGIN
    // ════════════════════════════════════════════════════════════════════

    @Override
    public ModuleLeaderLoginResponse login(ModuleLeaderLoginRequest req) {
        log.info("[MODULE LEADER LOGIN] Attempt email='{}'", req.getEmail());

        ModuleLeaderAccount account = accountRepository.findByUsername(req.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!account.isActive()) {
            throw new UnauthorizedException("Account is inactive. Contact the administrator.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        } catch (BadCredentialsException ex) {
            throw new UnauthorizedException("Invalid email or password");
        }

        var userDetails = User.builder()
                .username(account.getUsername())
                .password(account.getPassword())
                .authorities(new SimpleGrantedAuthority("ROLE_MODULE_LEADER"))
                .build();

        String token = jwtUtils.generateToken(userDetails, "ROLE_MODULE_LEADER");
        ModuleLeader ml = account.getModuleLeader();

        log.info("[MODULE LEADER LOGIN] Success email='{}'", req.getEmail());

        return ModuleLeaderLoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .moduleLeaderId(ml.getId())
                .firstName(ml.getFirstName())
                .lastName(ml.getLastName())
                .email(ml.getEmail())
                .subjectId(ml.getSubject().getId())
                .subjectName(ml.getSubject().getSubjectName())
                .subjectCode(ml.getSubject().getSubjectCode())
                .role("ROLE_MODULE_LEADER")
                .build();
    }

    // ════════════════════════════════════════════════════════════════════
    // MODULE LEADER DASHBOARD
    // ════════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public ModuleLeaderResponse getMyProfile(String email) {
        ModuleLeader ml = moduleLeaderRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("ModuleLeader", "email", email));
        return mapToResponse(ml, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimetableSlotDTO> getMySlotsSubject(String email) {
        UUID subjectId = getSubjectId(email);
        return slotRepository.findBySubjectId(subjectId)
                .stream().map(this::mapSlot).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceReportResponse getSlotReport(String email, UUID slotId, LocalDate date) {
        UUID subjectId = getSubjectId(email);
        TimetableSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("TimetableSlot", "id", slotId));

        // Security: module leader can only access their own subject
        if (!slot.getSubject().getId().equals(subjectId)) {
            throw new AppException(HttpStatus.FORBIDDEN,
                    "This slot does not belong to your subject.");
        }

        var records = attendanceRepository
                .findBySlotIdAndAttendanceDateOrderByMarkedAt(slotId, date);

        long totalEnrolled = enrollmentRepository
                .findBySectionIdAndStatus(slot.getSection().getId(), EnrollmentStatus.ACTIVE)
                .stream()
                .filter(e -> enrollmentRepository.isStudentEnrolledInSubject(
                        e.getStudent().getId(), subjectId))
                .count();

        long presentCount = records.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
        long absentCount  = records.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.ABSENT).count();
        double pct = totalEnrolled > 0
                ? Math.round((presentCount * 100.0 / totalEnrolled) * 10.0) / 10.0 : 0.0;

        return AttendanceReportResponse.builder()
                .slotId(slot.getId())
                .subjectName(slot.getSubject().getSubjectName())
                .subjectCode(slot.getSubject().getSubjectCode())
                .sectionName(slot.getSection().getName())
                .classroomName(slot.getClassroom().getName())
                .dayOfWeek(slot.getDayOfWeek().name())
                .startTime(slot.getStartTime().toString())
                .endTime(slot.getEndTime().toString())
                .date(date)
                .totalEnrolled((int) totalEnrolled)
                .presentCount((int) presentCount)
                .absentCount((int) absentCount)
                .attendancePercentage(pct)
                .records(records.stream().map(a -> {
                    String sName = a.getStudent().getFirstName()
                            + (a.getStudent().getLastName() != null
                                    ? " " + a.getStudent().getLastName() : "");
                    return AttendanceRecordResponse.builder()
                            .id(a.getId())
                            .studentId(a.getStudent().getId())
                            .studentName(sName)
                            .studentEmail(a.getStudent().getEmail())
                            .slotId(slotId)
                            .subjectName(slot.getSubject().getSubjectName())
                            .subjectCode(slot.getSubject().getSubjectCode())
                            .sectionName(slot.getSection().getName())
                            .classroomName(slot.getClassroom().getName())
                            .startTime(slot.getStartTime().toString())
                            .endTime(slot.getEndTime().toString())
                            .attendanceDate(a.getAttendanceDate())
                            .status(a.getStatus())
                            .markedAt(a.getMarkedAt())
                            .build();
                }).collect(Collectors.toList()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SlotHistoryResponse getSlotHistory(String email, UUID slotId) {
        UUID subjectId = getSubjectId(email);
        TimetableSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("TimetableSlot", "id", slotId));

        if (!slot.getSubject().getId().equals(subjectId)) {
            throw new AppException(HttpStatus.FORBIDDEN,
                    "This slot does not belong to your subject.");
        }

        List<LocalDate> sessionDates = attendanceRepository.findDistinctDatesBySlotId(slotId);

        long totalEnrolled = enrollmentRepository
                .findBySectionIdAndStatus(slot.getSection().getId(), EnrollmentStatus.ACTIVE)
                .stream()
                .filter(e -> enrollmentRepository.isStudentEnrolledInSubject(
                        e.getStudent().getId(), subjectId))
                .count();

        List<SlotHistoryResponse.SessionSummary> sessions = sessionDates.stream()
                .map(date -> {
                    var records = attendanceRepository
                            .findBySlotIdAndAttendanceDateOrderByMarkedAt(slotId, date);
                    long present = records.stream()
                            .filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
                    long absent  = records.stream()
                            .filter(r -> r.getStatus() == AttendanceStatus.ABSENT).count();
                    double pct = totalEnrolled > 0
                            ? Math.round((present * 100.0 / totalEnrolled) * 10.0) / 10.0 : 0.0;
                    return SlotHistoryResponse.SessionSummary.builder()
                            .date(date)
                            .totalEnrolled((int) totalEnrolled)
                            .presentCount((int) present)
                            .absentCount((int) absent)
                            .attendancePercentage(pct)
                            .build();
                })
                .collect(Collectors.toList());

        return SlotHistoryResponse.builder()
                .slotId(slot.getId())
                .subjectName(slot.getSubject().getSubjectName())
                .subjectCode(slot.getSubject().getSubjectCode())
                .sectionName(slot.getSection().getName())
                .classroomName(slot.getClassroom().getName())
                .dayOfWeek(slot.getDayOfWeek().name())
                .startTime(slot.getStartTime().toString())
                .endTime(slot.getEndTime().toString())
                .sessionDates(sessionDates)
                .sessions(sessions)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentAttendanceSummaryDTO> getStudentSummaries(String email) {
        ModuleLeader ml = moduleLeaderRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("ModuleLeader", "email", email));
        UUID subjectId = ml.getSubject().getId();

        List<TimetableSlot> slots = slotRepository.findBySubjectId(subjectId);
        Set<UUID> sectionIds = slots.stream()
                .map(s -> s.getSection().getId())
                .collect(Collectors.toSet());

        List<StudentAttendanceSummaryDTO> summaries = new ArrayList<>();
        Set<UUID> seen = new HashSet<>();

        for (UUID sectionId : sectionIds) {
            enrollmentRepository.findBySectionIdAndStatus(sectionId, EnrollmentStatus.ACTIVE)
                    .stream()
                    .filter(e -> enrollmentRepository.isStudentEnrolledInSubject(
                            e.getStudent().getId(), subjectId))
                    .forEach(e -> {
                        UUID studentId = e.getStudent().getId();
                        if (seen.add(studentId)) {
                            long total   = attendanceRepository
                                    .countTotalByStudentAndSubject(studentId, subjectId);
                            long present = attendanceRepository
                                    .countPresentByStudentAndSubject(studentId, subjectId);
                            long absent  = total - present;
                            double pct   = total > 0
                                    ? Math.round((present * 100.0 / total) * 10.0) / 10.0 : 0.0;

                            String studentName = e.getStudent().getFirstName()
                                    + (e.getStudent().getLastName() != null
                                            ? " " + e.getStudent().getLastName() : "");

                            summaries.add(StudentAttendanceSummaryDTO.builder()
                                    .studentId(studentId)
                                    .studentName(studentName)
                                    .studentEmail(e.getStudent().getEmail())
                                    .sectionName(e.getSection().getName())
                                    .subjectId(subjectId)
                                    .subjectName(ml.getSubject().getSubjectName())
                                    .subjectCode(ml.getSubject().getSubjectCode())
                                    .totalClasses(total)
                                    .presentCount(present)
                                    .absentCount(absent)
                                    .attendancePercentage(pct)
                                    .build());
                        }
                    });
        }

        summaries.sort(Comparator.comparing(StudentAttendanceSummaryDTO::getStudentName));
        return summaries;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeacherSummaryDTO> getTeachersForSubject(String email) {
        UUID subjectId = getSubjectId(email);

        List<TimetableSlot> slots = slotRepository.findBySubjectId(subjectId);

        // Group slots by teacher
        Map<UUID, List<TimetableSlot>> byTeacher = slots.stream()
                .collect(Collectors.groupingBy(s -> s.getTeacher().getId()));

        return byTeacher.entrySet().stream()
                .map(entry -> {
                    Teacher t = entry.getValue().get(0).getTeacher();
                    var fac = t.getFaculty();
                    return TeacherSummaryDTO.builder()
                            .teacherId(t.getId())
                            .firstName(fac.getFirstName())
                            .lastName(fac.getLastName())
                            .email(fac.getEmail())
                            .designation(fac.getDesignation())
                            .departmentName(fac.getDepartment() != null
                                    ? fac.getDepartment().getName() : null)
                            .slotCount(entry.getValue().size())
                            .build();
                })
                .sorted(Comparator.comparing(TeacherSummaryDTO::getFirstName))
                .collect(Collectors.toList());
    }

    // ════════════════════════════════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════════════════════════════════

    private UUID getSubjectId(String email) {
        return moduleLeaderRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("ModuleLeader", "email", email))
                .getSubject().getId();
    }

    private ModuleLeader findById(UUID id) {
        return moduleLeaderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ModuleLeader", "id", id));
    }

    private String generatePassword(String firstName, String phone, UUID id) {
        String prefix = firstName.toLowerCase().substring(0, Math.min(4, firstName.length()));
        String suffix;
        if (phone != null && !phone.isBlank()) {
            String digits = phone.replaceAll("\\D", "");
            suffix = digits.length() >= 4
                    ? digits.substring(digits.length() - 4)
                    : id.toString().replace("-", "").substring(0, 6);
        } else {
            suffix = id.toString().replace("-", "").substring(0, 6);
        }
        return prefix + "@" + suffix;
    }

    private ModuleLeaderResponse mapToResponse(ModuleLeader ml, String rawPassword) {
        return ModuleLeaderResponse.builder()
                .id(ml.getId())
                .firstName(ml.getFirstName())
                .lastName(ml.getLastName())
                .email(ml.getEmail())
                .phone(ml.getPhone())
                .subjectId(ml.getSubject().getId())
                .subjectName(ml.getSubject().getSubjectName())
                .subjectCode(ml.getSubject().getSubjectCode())
                .active(ml.isActive())
                .createdAt(ml.getCreatedAt())
                .generatedPassword(rawPassword)
                .build();
    }

    private TimetableSlotDTO mapSlot(TimetableSlot s) {
        return TimetableSlotDTO.builder()
                .id(s.getId())
                .teacherId(s.getTeacher().getId())
                .teacherName(s.getTeacher().getFaculty().getFirstName()
                        + " " + s.getTeacher().getFaculty().getLastName())
                .subjectId(s.getSubject().getId())
                .subjectName(s.getSubject().getSubjectName())
                .subjectCode(s.getSubject().getSubjectCode())
                .classroomId(s.getClassroom().getId())
                .classroomName(s.getClassroom().getName())
                .sectionId(s.getSection().getId())
                .sectionName(s.getSection().getName())
                .dayOfWeek(s.getDayOfWeek())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .effectiveFrom(s.getEffectiveFrom())
                .effectiveTo(s.getEffectiveTo())
                .notes(s.getNotes())
                .build();
    }
}
