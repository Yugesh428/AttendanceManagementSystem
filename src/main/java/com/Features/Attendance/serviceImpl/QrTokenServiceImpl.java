package com.Features.Attendance.serviceImpl;

import com.Features.Attendance.dto.QrTokenResponse;
import com.Features.Attendance.model.QrToken;
import com.Features.Attendance.model.QrTokenStatus;
import com.Features.Attendance.repository.QrTokenRepository;
import com.Features.Attendance.service.QrTokenService;
import com.Features.Teacher.model.Teacher;
import com.Features.Teacher.repository.TeacherRepository;
import com.Features.Timetable.model.TimetableSlot;
import com.Features.Timetable.repository.TimetableSlotRepository;
import com.exception.AppException;
import com.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class QrTokenServiceImpl implements QrTokenService {

    private final QrTokenRepository     qrTokenRepository;
    private final TimetableSlotRepository slotRepository;
    private final TeacherRepository     teacherRepository;

    /**
     * How long each QR token is valid (minutes). Configurable.
     * Default = 2 minutes.
     */
    @Value("${app.qr.token-validity-minutes:2}")
    private int tokenValidityMinutes;

    /**
     * Base URL for QR scan links.
     * Students open this URL when they scan the QR code.
     */
    @Value("${app.qr.base-url:http://localhost:8080/attend}")
    private String qrBaseUrl;

    /**
     * Auto-rotate when token has fewer than this many seconds left.
     * Default = 30 seconds.
     */
    @Value("${app.qr.rotation-threshold-seconds:30}")
    private int rotationThresholdSeconds;

    // ════════════════════════════════════════════════════════════════════
    // START SESSION
    // ════════════════════════════════════════════════════════════════════

    @Override
    public QrTokenResponse startSession(UUID slotId, String teacherEmail) {
        log.info("[QR] Starting session slotId='{}' teacher='{}'", slotId, teacherEmail);

        TimetableSlot slot = findSlot(slotId);
        Teacher teacher    = findTeacher(teacherEmail);

        // Only the teacher assigned to this slot can start it
        if (!slot.getTeacher().getId().equals(teacher.getId())) {
            throw new AppException(HttpStatus.FORBIDDEN,
                    "You are not the teacher assigned to this slot.");
        }

        LocalDate today = LocalDate.now();

        // Prevent duplicate session for same slot on same day
        if (qrTokenRepository.existsBySlotIdAndSessionDateAndStatus(
                slotId, today, QrTokenStatus.ACTIVE)) {
            throw new AppException(HttpStatus.CONFLICT,
                    "An attendance session is already active for this slot today. "
                    + "Use GET /api/teacher/qr/{slotId}/current to retrieve it.");
        }

        UUID sessionId = UUID.randomUUID();
        QrToken token  = buildNewToken(slot, teacher, today, sessionId, 0);
        QrToken saved  = qrTokenRepository.save(token);

        log.info("[QR] Session started tokenId='{}' sessionId='{}' expiresAt='{}'",
                saved.getId(), sessionId, saved.getExpiresAt());

        return mapToResponse(saved);
    }

    // ════════════════════════════════════════════════════════════════════
    // GET CURRENT TOKEN (teacher polls this)
    // ════════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public QrTokenResponse getCurrentToken(UUID slotId) {
        QrToken token = qrTokenRepository
                .findBySlotIdAndSessionDateAndStatus(slotId, LocalDate.now(), QrTokenStatus.ACTIVE)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "No active attendance session for this slot today. "
                        + "Start one with POST /api/teacher/qr/{slotId}/start"));
        return mapToResponse(token);
    }

    // ════════════════════════════════════════════════════════════════════
    // MANUAL ROTATE (teacher force-rotates)
    // ════════════════════════════════════════════════════════════════════

    @Override
    public QrTokenResponse rotateToken(UUID slotId, String teacherEmail) {
        log.info("[QR] Manual rotation slotId='{}' teacher='{}'", slotId, teacherEmail);

        QrToken current = qrTokenRepository
                .findBySlotIdAndSessionDateAndStatus(slotId, LocalDate.now(), QrTokenStatus.ACTIVE)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "No active session found for this slot."));

        Teacher teacher = findTeacher(teacherEmail);
        if (!current.getSlot().getTeacher().getId().equals(teacher.getId())) {
            throw new AppException(HttpStatus.FORBIDDEN,
                    "You are not the teacher assigned to this slot.");
        }

        return rotate(current);
    }

    // ════════════════════════════════════════════════════════════════════
    // END SESSION
    // ════════════════════════════════════════════════════════════════════

    @Override
    public void endSession(UUID slotId, String teacherEmail) {
        log.info("[QR] Ending session slotId='{}' teacher='{}'", slotId, teacherEmail);

        QrToken current = qrTokenRepository
                .findBySlotIdAndSessionDateAndStatus(slotId, LocalDate.now(), QrTokenStatus.ACTIVE)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "No active session found for this slot."));

        Teacher teacher = findTeacher(teacherEmail);
        if (!current.getSlot().getTeacher().getId().equals(teacher.getId())) {
            throw new AppException(HttpStatus.FORBIDDEN,
                    "You are not the teacher assigned to this slot.");
        }

        current.setStatus(QrTokenStatus.REVOKED);
        qrTokenRepository.save(current);

        log.info("[QR] Session ended tokenId='{}' sessionId='{}'",
                current.getId(), current.getSessionId());
    }

    // ════════════════════════════════════════════════════════════════════
    // ADMIN — list all active tokens
    // ════════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<QrTokenResponse> getAllActiveTokens() {
        return qrTokenRepository.findByStatusOrderByCreatedAtDesc(QrTokenStatus.ACTIVE)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ════════════════════════════════════════════════════════════════════
    // ADMIN — revoke any token
    // ════════════════════════════════════════════════════════════════════

    @Override
    public void revokeToken(UUID tokenId) {
        log.info("[QR] Admin revoking tokenId='{}'", tokenId);
        QrToken token = qrTokenRepository.findById(tokenId)
                .orElseThrow(() -> new ResourceNotFoundException("QrToken", "id", tokenId));
        token.setStatus(QrTokenStatus.REVOKED);
        qrTokenRepository.save(token);
    }

    // ════════════════════════════════════════════════════════════════════
    // SCHEDULED AUTO-ROTATION
    // Runs every 30 seconds. Rotates any ACTIVE token within
    // rotationThresholdSeconds of expiry.
    // ════════════════════════════════════════════════════════════════════

    @Override
    @Scheduled(fixedDelayString = "${app.qr.rotation-check-interval-ms:30000}")
    public void autoRotateExpiringTokens() {
        LocalDateTime threshold = LocalDateTime.now()
                .plusSeconds(rotationThresholdSeconds);

        List<QrToken> expiring = qrTokenRepository.findTokensNearingExpiry(threshold);

        if (!expiring.isEmpty()) {
            log.info("[QR] Auto-rotating {} token(s) nearing expiry", expiring.size());
            expiring.forEach(this::rotate);
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ════════════════════════════════════════════════════════════════════

    /** Expire the current token and generate a fresh replacement */
    private QrTokenResponse rotate(QrToken current) {
        current.setStatus(QrTokenStatus.EXPIRED);
        qrTokenRepository.save(current);

        QrToken fresh = buildNewToken(
                current.getSlot(),
                current.getGeneratedBy(),
                current.getSessionDate(),
                current.getSessionId(),
                current.getRotationCount() + 1);

        QrToken saved = qrTokenRepository.save(fresh);

        log.info("[QR] Rotated → old='{}' new='{}' rotation=#{}",
                current.getId(), saved.getId(), saved.getRotationCount());

        return mapToResponse(saved);
    }

    private QrToken buildNewToken(TimetableSlot slot, Teacher teacher,
                                   LocalDate sessionDate, UUID sessionId, int rotationCount) {
        return QrToken.builder()
                .tokenValue(UUID.randomUUID().toString())
                .slot(slot)
                .sessionDate(sessionDate)
                .generatedBy(teacher)
                .expiresAt(LocalDateTime.now().plusMinutes(tokenValidityMinutes))
                .status(QrTokenStatus.ACTIVE)
                .sessionId(sessionId)
                .rotationCount(rotationCount)
                .build();
    }

    private QrTokenResponse mapToResponse(QrToken t) {
        long secondsLeft = ChronoUnit.SECONDS.between(LocalDateTime.now(), t.getExpiresAt());
        TimetableSlot slot = t.getSlot();

        return QrTokenResponse.builder()
                .id(t.getId())
                .tokenValue(t.getTokenValue())
                .sessionId(t.getSessionId())
                .rotationCount(t.getRotationCount())
                .slotId(slot.getId())
                .subjectName(slot.getSubject().getSubjectName())
                .subjectCode(slot.getSubject().getSubjectCode())
                .classroomName(slot.getClassroom().getName())
                .sectionName(slot.getSection().getName())
                .dayOfWeek(slot.getDayOfWeek().name())
                .startTime(slot.getStartTime().toString())
                .endTime(slot.getEndTime().toString())
                .sessionDate(t.getSessionDate())
                .expiresAt(t.getExpiresAt())
                .createdAt(t.getCreatedAt())
                .status(t.getStatus())
                .secondsUntilExpiry(Math.max(0, secondsLeft))
                .qrUrl(qrBaseUrl + "?token=" + t.getTokenValue())
                .build();
    }

    private TimetableSlot findSlot(UUID id) {
        return slotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TimetableSlot", "id", id));
    }

    private Teacher findTeacher(String email) {
        return teacherRepository.findByFacultyEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "email", email));
    }
}
