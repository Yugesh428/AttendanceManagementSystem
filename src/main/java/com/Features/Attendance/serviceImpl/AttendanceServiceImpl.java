package com.Features.Attendance.serviceImpl;

import com.Features.Admin.Beacon.Beacon;
import com.Features.Admin.Beacon.Repository.BeaconRepository;
import com.Features.Admin.Student.model.Student;
import com.Features.Admin.Student.model.StudentAccount;
import com.Features.Admin.Student.repository.StudentAccountRepository;
import com.Features.Admin.Student.repository.StudentRepository;
import com.Features.Attendance.dto.AttendanceRecordResponse;
import com.Features.Attendance.dto.AttendanceReportResponse;
import com.Features.Attendance.dto.AttendanceScanRequest;
import com.Features.Attendance.dto.SlotHistoryResponse;
import com.Features.Attendance.dto.SlotStudentResponse;
import com.Features.Attendance.model.AttendanceRecord;
import com.Features.Attendance.model.AttendanceStatus;
import com.Features.Attendance.model.QrToken;
import com.Features.Attendance.model.QrTokenStatus;
import com.Features.Attendance.repository.AttendanceRepository;
import com.Features.Attendance.repository.QrTokenRepository;
import com.Features.Attendance.service.AttendanceService;
import com.Features.Enrollment.model.Enrollment;
import com.Features.Enrollment.model.EnrollmentStatus;
import com.Features.Enrollment.repository.EnrollmentRepository;
import com.Features.Timetable.model.TimetableSlot;
import com.Features.Timetable.repository.TimetableSlotRepository;
import com.exception.AppException;
import com.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository      attendanceRepository;
    private final QrTokenRepository         qrTokenRepository;
    private final StudentRepository         studentRepository;
    private final StudentAccountRepository  accountRepository;
    private final EnrollmentRepository      enrollmentRepository;
    private final BeaconRepository          beaconRepository;
    private final TimetableSlotRepository   slotRepository;

    // ════════════════════════════════════════════════════════════════════
    // SCAN — 6-step validation then mark PRESENT
    // ════════════════════════════════════════════════════════════════════

    @Override
    public AttendanceRecordResponse scan(AttendanceScanRequest request, String studentEmail) {
        log.info("[ATTENDANCE] Scan student='{}' token='{}' beacon='{}'",
                studentEmail, request.getQrToken(), request.getBeaconId());

        // Load student
        Student student = studentRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "email", studentEmail));

        StudentAccount account = accountRepository.findByStudentId(student.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "StudentAccount", "studentId", student.getId()));

        // ── Validation 1: QR token valid, ACTIVE, not expired ─────────────────
        QrToken qrToken = qrTokenRepository.findByTokenValue(request.getQrToken())
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST,
                        "Invalid QR code. Scan the latest code displayed by your teacher."));

        if (qrToken.getStatus() == QrTokenStatus.REVOKED) {
            throw new AppException(HttpStatus.GONE,
                    "This attendance session has ended.");
        }
        if (qrToken.getStatus() == QrTokenStatus.EXPIRED
                || qrToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException(HttpStatus.GONE,
                    "This QR code has expired. Scan the latest code on screen.");
        }

        TimetableSlot slot = qrToken.getSlot();

        // ── Validation 2: Device ID matches registered phone ──────────────────
        if (account.getDeviceId() == null) {
            throw new AppException(HttpStatus.FORBIDDEN,
                    "No phone registered. Log in from your mobile phone first.");
        }
        if (!account.getDeviceId().equals(request.getDeviceId())) {
            throw new AppException(HttpStatus.FORBIDDEN,
                    "Device mismatch. Attendance must be marked from your registered phone.");
        }

        // ── Validation 3: Beacon matches classroom ────────────────────────────
        List<Beacon> classroomBeacons =
                beaconRepository.findByClassroomId(slot.getClassroom().getId());

        boolean beaconValid = classroomBeacons.stream()
                .anyMatch(b -> b.getUuid().equalsIgnoreCase(request.getBeaconId()));

        if (!beaconValid) {
            throw new AppException(HttpStatus.FORBIDDEN,
                    "Beacon validation failed. You must be physically present in the classroom.");
        }

        // ── Validation 4: Student section matches slot section ────────────────
        Enrollment activeEnrollment = enrollmentRepository
                .findByStudentIdAndStatus(student.getId(), EnrollmentStatus.ACTIVE)
                .orElseThrow(() -> new AppException(HttpStatus.FORBIDDEN,
                        "You are not enrolled in any active semester."));

        if (!activeEnrollment.getSection().getId().equals(slot.getSection().getId())) {
            throw new AppException(HttpStatus.FORBIDDEN,
                    "This class does not belong to your section.");
        }

        // ── Validation 5: Student enrolled in this subject ────────────────────
        if (!enrollmentRepository.isStudentEnrolledInSubject(
                student.getId(), slot.getSubject().getId())) {
            throw new AppException(HttpStatus.FORBIDDEN,
                    "You are not enrolled in '" + slot.getSubject().getSubjectName() + "'.");
        }

        // ── Validation 6: No duplicate attendance ─────────────────────────────
        LocalDate sessionDate = qrToken.getSessionDate();
        if (attendanceRepository.existsByStudentIdAndSlotIdAndAttendanceDate(
                student.getId(), slot.getId(), sessionDate)) {
            throw new AppException(HttpStatus.CONFLICT,
                    "Attendance already marked for this class today.");
        }

        // ── Save PRESENT record ───────────────────────────────────────────────
        AttendanceRecord saved = attendanceRepository.save(AttendanceRecord.builder()
                .student(student)
                .slot(slot)
                .attendanceDate(sessionDate)
                .status(AttendanceStatus.PRESENT)
                .deviceId(request.getDeviceId())
                .beaconId(request.getBeaconId())
                .qrToken(qrToken)
                .build());

        log.info("[ATTENDANCE] PRESENT studentId='{}' slotId='{}' date='{}'",
                student.getId(), slot.getId(), sessionDate);

        return mapToResponse(saved);
    }

    // ════════════════════════════════════════════════════════════════════
    // MARK ABSENTEES — call when teacher ends the session
    // ════════════════════════════════════════════════════════════════════

    @Override
    public void markAbsentees(UUID slotId, LocalDate date) {
        log.info("[ATTENDANCE] Marking absentees slotId='{}' date='{}'", slotId, date);

        TimetableSlot slot = findSlot(slotId);

        List<Enrollment> enrolled = enrollmentRepository
                .findBySectionIdAndStatus(slot.getSection().getId(), EnrollmentStatus.ACTIVE);

        int absentCount = 0;
        for (Enrollment enrollment : enrolled) {
            UUID studentId = enrollment.getStudent().getId();

            if (!enrollmentRepository.isStudentEnrolledInSubject(
                    studentId, slot.getSubject().getId())) continue;

            if (attendanceRepository.existsByStudentIdAndSlotIdAndAttendanceDate(
                    studentId, slotId, date)) continue;

            attendanceRepository.save(AttendanceRecord.builder()
                    .student(enrollment.getStudent())
                    .slot(slot)
                    .attendanceDate(date)
                    .status(AttendanceStatus.ABSENT)
                    .build());
            absentCount++;
        }

        log.info("[ATTENDANCE] Marked {} absent slotId='{}' date='{}'", absentCount, slotId, date);
    }

    // ════════════════════════════════════════════════════════════════════
    // REPORT — per slot per date
    // ════════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public AttendanceReportResponse getReport(UUID slotId, LocalDate date) {
        TimetableSlot slot = findSlot(slotId);

        List<AttendanceRecord> records = attendanceRepository
                .findBySlotIdAndAttendanceDateOrderByMarkedAt(slotId, date);

        long totalEnrolled = enrollmentRepository
                .findBySectionIdAndStatus(slot.getSection().getId(), EnrollmentStatus.ACTIVE)
                .stream()
                .filter(e -> enrollmentRepository.isStudentEnrolledInSubject(
                        e.getStudent().getId(), slot.getSubject().getId()))
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
                .records(records.stream().map(this::mapToResponse).collect(Collectors.toList()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceRecordResponse> getBySection(UUID sectionId, LocalDate date) {
        return attendanceRepository.findBySectionAndDate(sectionId, date)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceRecordResponse> getMyAttendance(String studentEmail,
                                                           LocalDate from, LocalDate to) {
        Student student = studentRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "email", studentEmail));

        return attendanceRepository
                .findByStudentIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(
                        student.getId(), from, to)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceReportResponse.SubjectAttendanceSummary> getMySubjectSummary(
            String studentEmail) {

        Student student = studentRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "email", studentEmail));

        Enrollment enrollment = enrollmentRepository
                .findByStudentIdAndStatus(student.getId(), EnrollmentStatus.ACTIVE)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "No active enrollment found."));

        List<AttendanceReportResponse.SubjectAttendanceSummary> summaries = new ArrayList<>();

        for (var subject : enrollment.getSubjects()) {
            long total   = attendanceRepository.countTotalByStudentAndSubject(
                    student.getId(), subject.getId());
            long present = attendanceRepository.countPresentByStudentAndSubject(
                    student.getId(), subject.getId());
            double pct = total > 0
                    ? Math.round((present * 100.0 / total) * 10.0) / 10.0 : 0.0;

            summaries.add(AttendanceReportResponse.SubjectAttendanceSummary.builder()
                    .subjectId(subject.getId())
                    .subjectName(subject.getSubjectName())
                    .subjectCode(subject.getSubjectCode())
                    .totalClasses(total)
                    .presentCount(present)
                    .percentage(pct)
                    .build());
        }

        return summaries;
    }

    // ════════════════════════════════════════════════════════════════════
    // ADMIN OVERRIDE
    // ════════════════════════════════════════════════════════════════════

    @Override
    public AttendanceRecordResponse override(UUID studentId, UUID slotId,
                                              LocalDate date, String status, String reason) {
        log.info("[ATTENDANCE] Override studentId='{}' slotId='{}' date='{}' → '{}'",
                studentId, slotId, date, status);

        AttendanceStatus newStatus;
        try {
            newStatus = AttendanceStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Invalid status '" + status + "'. Use PRESENT, ABSENT, or LATE.");
        }

        Optional<AttendanceRecord> existing = attendanceRepository
                .findByStudentIdAndSlotIdAndAttendanceDate(studentId, slotId, date);

        AttendanceRecord record;
        if (existing.isPresent()) {
            record = existing.get();
            record.setStatus(newStatus);
        } else {
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));
            TimetableSlot slot = findSlot(slotId);
            record = AttendanceRecord.builder()
                    .student(student)
                    .slot(slot)
                    .attendanceDate(date)
                    .status(newStatus)
                    .build();
        }

        return mapToResponse(attendanceRepository.save(record));
    }

    // ════════════════════════════════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════════════════════════════════

    private TimetableSlot findSlot(UUID id) {
        return slotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TimetableSlot", "id", id));
    }

    private AttendanceRecordResponse mapToResponse(AttendanceRecord a) {
        TimetableSlot slot = a.getSlot();
        String studentName = a.getStudent().getFirstName()
                + (a.getStudent().getLastName() != null
                        ? " " + a.getStudent().getLastName() : "");
        return AttendanceRecordResponse.builder()
                .id(a.getId())
                .studentId(a.getStudent().getId())
                .studentName(studentName)
                .studentEmail(a.getStudent().getEmail())
                .slotId(slot.getId())
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
    }

    // ════════════════════════════════════════════════════════════════════
    // STUDENTS FOR SLOT — teacher sees who is in their class
    // ════════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<SlotStudentResponse> getStudentsForSlot(UUID slotId) {
        TimetableSlot slot = findSlot(slotId);

        // All students actively enrolled in this slot's section
        // who are also enrolled in this slot's subject
        return enrollmentRepository
                .findBySectionIdAndStatus(slot.getSection().getId(), EnrollmentStatus.ACTIVE)
                .stream()
                .filter(e -> enrollmentRepository.isStudentEnrolledInSubject(
                        e.getStudent().getId(), slot.getSubject().getId()))
                .map(e -> {
                    var s = e.getStudent();
                    return SlotStudentResponse.builder()
                            .studentId(s.getId())
                            .firstName(s.getFirstName())
                            .lastName(s.getLastName())
                            .email(s.getEmail())
                            .phone(s.getPhone())
                            .build();
                })
                .sorted((a, b) -> a.getFirstName().compareToIgnoreCase(b.getFirstName()))
                .collect(Collectors.toList());
    }

    // ════════════════════════════════════════════════════════════════════
    // SLOT HISTORY — teacher sees all past sessions for a slot
    // ════════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public SlotHistoryResponse getSlotHistory(UUID slotId) {
        TimetableSlot slot = findSlot(slotId);

        // All distinct dates this slot has had attendance
        List<LocalDate> sessionDates = attendanceRepository.findDistinctDatesBySlotId(slotId);

        // Total enrolled students for this slot
        long totalEnrolled = enrollmentRepository
                .findBySectionIdAndStatus(slot.getSection().getId(), EnrollmentStatus.ACTIVE)
                .stream()
                .filter(e -> enrollmentRepository.isStudentEnrolledInSubject(
                        e.getStudent().getId(), slot.getSubject().getId()))
                .count();

        // Build a SessionSummary for each date — newest first
        List<SlotHistoryResponse.SessionSummary> sessions = sessionDates.stream()
                .map(date -> {
                    List<AttendanceRecord> records =
                            attendanceRepository.findBySlotIdAndAttendanceDateOrderByMarkedAt(slotId, date);

                    long presentCount = records.stream()
                            .filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
                    long absentCount  = records.stream()
                            .filter(r -> r.getStatus() == AttendanceStatus.ABSENT).count();
                    double pct = totalEnrolled > 0
                            ? Math.round((presentCount * 100.0 / totalEnrolled) * 10.0) / 10.0 : 0.0;

                    return SlotHistoryResponse.SessionSummary.builder()
                            .date(date)
                            .totalEnrolled((int) totalEnrolled)
                            .presentCount((int) presentCount)
                            .absentCount((int) absentCount)
                            .attendancePercentage(pct)
                            .records(records.stream().map(this::mapToResponse).collect(Collectors.toList()))
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
}
