package com.Features.FacultyAttendance.serviceImpl;

import com.Features.Admin.faculty.Faculty;
import com.Features.Admin.faculty.reposityory.FacultyRepository;
import com.Features.FacultyAttendance.dto.*;
import com.Features.FacultyAttendance.model.*;
import com.Features.FacultyAttendance.repository.FacultyAttendanceRepository;
import com.Features.FacultyAttendance.repository.FacultyDailyQrRepository;
import com.Features.FacultyAttendance.service.FacultyAttendanceService;
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
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FacultyAttendanceServiceImpl implements FacultyAttendanceService {

    private final FacultyDailyQrRepository    qrRepository;
    private final FacultyAttendanceRepository attendanceRepository;
    private final FacultyRepository           facultyRepository;

    @Value("${app.faculty.allowed-network:0.0.0.0/0}")
    private String allowedNetwork;

    @Value("${app.faculty.qr-validity-hours:24}")
    private int qrValidityHours;

    @Value("${app.qr.base-url:http://localhost:8080/attend}")
    private String qrBaseUrl;

    // ════════════════════════════════════════════════════════════════════
    // GENERATE DAILY QR
    // ════════════════════════════════════════════════════════════════════

    @Override
    public FacultyDailyQrResponse generateDailyQr(LocalDate date, String adminEmail) {
        log.info("[FACULTY-QR] Generating QR for date='{}' by admin='{}'", date, adminEmail);

        if (qrRepository.existsByQrDate(date)) {
            // Return existing if already generated
            FacultyDailyQr existing = qrRepository.findByQrDate(date).orElseThrow();
            log.info("[FACULTY-QR] QR already exists for date='{}' — returning existing", date);
            return mapQrToResponse(existing);
        }

        LocalDateTime expiresAt = date.atTime(23, 59, 59); // valid until end of day

        FacultyDailyQr qr = qrRepository.save(FacultyDailyQr.builder()
                .qrDate(date)
                .tokenValue(UUID.randomUUID().toString())
                .expiresAt(expiresAt)
                .status(FacultyQrStatus.ACTIVE)
                .generatedBy(adminEmail)
                .build());

        log.info("[FACULTY-QR] Created id='{}' for date='{}'", qr.getId(), date);
        return mapQrToResponse(qr);
    }

    // ════════════════════════════════════════════════════════════════════
    // GET TODAY'S QR
    // ════════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public FacultyDailyQrResponse getTodayQr() {
        FacultyDailyQr qr = qrRepository
                .findByQrDateAndStatus(LocalDate.now(), FacultyQrStatus.ACTIVE)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "No active QR for today. Ask admin to generate the daily QR."));
        return mapQrToResponse(qr);
    }

    // ════════════════════════════════════════════════════════════════════
    // REVOKE QR
    // ════════════════════════════════════════════════════════════════════

    @Override
    public void revokeQr(UUID qrId) {
        log.info("[FACULTY-QR] Revoking qrId='{}'", qrId);
        FacultyDailyQr qr = qrRepository.findById(qrId)
                .orElseThrow(() -> new ResourceNotFoundException("FacultyDailyQr", "id", qrId));
        qr.setStatus(FacultyQrStatus.REVOKED);
        qrRepository.save(qr);
    }

    // ════════════════════════════════════════════════════════════════════
    // FACULTY SCAN
    // ════════════════════════════════════════════════════════════════════

    @Override
    public FacultyAttendanceRecordResponse scan(String qrToken, String facultyEmail,
                                                 String clientIp) {
        log.info("[FACULTY-ATTENDANCE] Scan faculty='{}' ip='{}'", facultyEmail, clientIp);

        // ── Load faculty ──────────────────────────────────────────────────────
        Faculty faculty = facultyRepository.findByEmail(facultyEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty", "email", facultyEmail));

        // ── Validation 1: WiFi network check ──────────────────────────────────
        if (!isIpAllowed(clientIp)) {
            log.warn("[FACULTY-ATTENDANCE] IP '{}' not on college network", clientIp);
            throw new AppException(HttpStatus.FORBIDDEN,
                    "You must be connected to the college WiFi network to mark attendance. " +
                    "Current IP: " + clientIp);
        }

        // ── Validation 2: QR exists ───────────────────────────────────────────
        FacultyDailyQr qr = qrRepository.findByTokenValue(qrToken)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST,
                        "Invalid QR code. Please scan the QR displayed by admin."));

        // ── Validation 3: QR not revoked ──────────────────────────────────────
        if (qr.getStatus() == FacultyQrStatus.REVOKED) {
            throw new AppException(HttpStatus.GONE,
                    "This QR has been revoked by admin.");
        }

        // ── Validation 4: QR not expired ─────────────────────────────────────
        if (qr.getStatus() == FacultyQrStatus.EXPIRED
                || qr.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException(HttpStatus.GONE,
                    "Today's QR has expired. It is valid only for " + qr.getQrDate());
        }

        // ── Validation 5: No duplicate ────────────────────────────────────────
        LocalDate today = qr.getQrDate();
        if (attendanceRepository.existsByFacultyIdAndAttendanceDate(faculty.getId(), today)) {
            throw new AppException(HttpStatus.CONFLICT,
                    "Attendance already marked for today (" + today + ").");
        }

        // ── Save PRESENT record ───────────────────────────────────────────────
        FacultyAttendanceRecord saved = attendanceRepository.save(
                FacultyAttendanceRecord.builder()
                        .faculty(faculty)
                        .attendanceDate(today)
                        .status(FacultyAttendanceStatus.PRESENT)
                        .scannedFromIp(clientIp)
                        .qr(qr)
                        .build());

        log.info("[FACULTY-ATTENDANCE] PRESENT facultyId='{}' date='{}'",
                faculty.getId(), today);
        return mapToResponse(saved);
    }

    // ════════════════════════════════════════════════════════════════════
    // REPORTS
    // ════════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public FacultyAttendanceReportResponse getDailyReport(LocalDate date) {
        List<FacultyAttendanceRecord> records =
                attendanceRepository.findByAttendanceDateOrderByFacultyFirstName(date);

        long totalFaculty = facultyRepository.count();
        long present = records.stream()
                .filter(r -> r.getStatus() == FacultyAttendanceStatus.PRESENT).count();
        long absent = totalFaculty - present;
        double pct = totalFaculty > 0
                ? Math.round((present * 100.0 / totalFaculty) * 10.0) / 10.0 : 0.0;

        return FacultyAttendanceReportResponse.builder()
                .date(date)
                .totalFaculty((int) totalFaculty)
                .presentCount((int) present)
                .absentCount((int) absent)
                .attendancePercentage(pct)
                .records(records.stream().map(this::mapToResponse).collect(Collectors.toList()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public FacultyAttendanceReportResponse getDepartmentReport(UUID departmentId, LocalDate date) {
        List<FacultyAttendanceRecord> records =
                attendanceRepository.findByDepartmentAndDate(departmentId, date);

        long totalInDept = facultyRepository.findAll().stream()
                .filter(f -> f.getDepartment() != null
                        && f.getDepartment().getId().equals(departmentId))
                .count();

        long present = records.stream()
                .filter(r -> r.getStatus() == FacultyAttendanceStatus.PRESENT).count();
        long absent = totalInDept - present;
        double pct = totalInDept > 0
                ? Math.round((present * 100.0 / totalInDept) * 10.0) / 10.0 : 0.0;

        return FacultyAttendanceReportResponse.builder()
                .date(date)
                .totalFaculty((int) totalInDept)
                .presentCount((int) present)
                .absentCount((int) absent)
                .attendancePercentage(pct)
                .records(records.stream().map(this::mapToResponse).collect(Collectors.toList()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacultyAttendanceRecordResponse> getFacultyHistory(UUID facultyId,
                                                                    LocalDate from,
                                                                    LocalDate to) {
        if (!facultyRepository.existsById(facultyId)) {
            throw new ResourceNotFoundException("Faculty", "id", facultyId);
        }
        return attendanceRepository
                .findByFacultyIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(
                        facultyId, from, to)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacultyAttendanceRecordResponse> getMyHistory(String facultyEmail,
                                                               LocalDate from, LocalDate to) {
        Faculty faculty = facultyRepository.findByEmail(facultyEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty", "email", facultyEmail));
        return attendanceRepository
                .findByFacultyIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(
                        faculty.getId(), from, to)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ════════════════════════════════════════════════════════════════════
    // ADMIN OVERRIDE
    // ════════════════════════════════════════════════════════════════════

    @Override
    public FacultyAttendanceRecordResponse override(UUID facultyId, LocalDate date,
                                                     String status, String reason) {
        log.info("[FACULTY-ATTENDANCE] Override facultyId='{}' date='{}' → '{}'",
                facultyId, date, status);

        FacultyAttendanceStatus newStatus;
        try {
            newStatus = FacultyAttendanceStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Invalid status '" + status + "'. Use PRESENT, ABSENT, LATE, or LEAVE.");
        }

        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty", "id", facultyId));

        Optional<FacultyAttendanceRecord> existing =
                attendanceRepository.findByFacultyIdAndAttendanceDate(facultyId, date);

        FacultyAttendanceRecord record;
        if (existing.isPresent()) {
            record = existing.get();
            record.setStatus(newStatus);
        } else {
            record = FacultyAttendanceRecord.builder()
                    .faculty(faculty)
                    .attendanceDate(date)
                    .status(newStatus)
                    .build();
        }

        return mapToResponse(attendanceRepository.save(record));
    }

    // ════════════════════════════════════════════════════════════════════
    // SCHEDULED: expire old QR codes at midnight
    // ════════════════════════════════════════════════════════════════════

    @Override
    @Scheduled(cron = "0 1 0 * * *")  // runs at 00:01 every day
    public void expireOldQrCodes() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        qrRepository.findByQrDate(yesterday).ifPresent(qr -> {
            if (qr.getStatus() == FacultyQrStatus.ACTIVE) {
                qr.setStatus(FacultyQrStatus.EXPIRED);
                qrRepository.save(qr);
                log.info("[FACULTY-QR] Expired QR for date='{}'", yesterday);
            }
        });
    }

    // ════════════════════════════════════════════════════════════════════
    // WIFI VALIDATION
    // ════════════════════════════════════════════════════════════════════

    /**
     * Checks if the given IP address falls within the allowed college network.
     * Supports CIDR notation (e.g. 192.168.1.0/24).
     * If allowed-network is 0.0.0.0/0 — all IPs are accepted (testing mode).
     */
    private boolean isIpAllowed(String clientIp) {
        if (allowedNetwork == null || allowedNetwork.equals("0.0.0.0/0")) {
            return true; // open for testing
        }
        try {
            return isInCidr(clientIp, allowedNetwork);
        } catch (Exception e) {
            log.warn("[FACULTY-ATTENDANCE] Could not validate IP '{}': {}", clientIp, e.getMessage());
            return false;
        }
    }

    /**
     * Checks if an IP is within a CIDR range.
     * Works for IPv4 only.
     */
    private boolean isInCidr(String ip, String cidr) {
        String[] parts = cidr.split("/");
        String networkAddress = parts[0];
        int prefixLength = Integer.parseInt(parts[1]);

        long networkLong = ipToLong(networkAddress);
        long ipLong      = ipToLong(ip);
        long mask        = prefixLength == 0 ? 0L : (~0L << (32 - prefixLength)) & 0xFFFFFFFFL;

        return (ipLong & mask) == (networkLong & mask);
    }

    private long ipToLong(String ip) {
        // Handle IPv4-mapped IPv6 addresses (e.g. ::1 or 0:0:0:0:0:0:0:1 = localhost)
        if (ip.contains(":")) {
            // IPv6 loopback — treat as 127.0.0.1 for local testing
            return ipToLong("127.0.0.1");
        }
        String[] octets = ip.split("\\.");
        long result = 0;
        for (String octet : octets) {
            result = result * 256 + Long.parseLong(octet);
        }
        return result;
    }

    // ════════════════════════════════════════════════════════════════════
    // MAPPERS
    // ════════════════════════════════════════════════════════════════════

    private FacultyDailyQrResponse mapQrToResponse(FacultyDailyQr qr) {
        long secondsLeft = ChronoUnit.SECONDS.between(LocalDateTime.now(), qr.getExpiresAt());
        return FacultyDailyQrResponse.builder()
                .id(qr.getId())
                .qrDate(qr.getQrDate())
                .tokenValue(qr.getTokenValue())
                .expiresAt(qr.getExpiresAt())
                .status(qr.getStatus())
                .generatedBy(qr.getGeneratedBy())
                .createdAt(qr.getCreatedAt())
                .secondsUntilExpiry(Math.max(0, secondsLeft))
                .qrUrl(qrBaseUrl + "/faculty?token=" + qr.getTokenValue())
                .build();
    }

    private FacultyAttendanceRecordResponse mapToResponse(FacultyAttendanceRecord a) {
        Faculty f = a.getFaculty();
        String name = f.getFirstName()
                + (f.getMiddleName() != null ? " " + f.getMiddleName() : "")
                + (f.getLastName() != null ? " " + f.getLastName() : "");
        return FacultyAttendanceRecordResponse.builder()
                .id(a.getId())
                .facultyId(f.getId())
                .facultyName(name)
                .facultyEmail(f.getEmail())
                .designation(f.getDesignation())
                .departmentName(f.getDepartment() != null ? f.getDepartment().getName() : null)
                .attendanceDate(a.getAttendanceDate())
                .status(a.getStatus())
                .scannedFromIp(a.getScannedFromIp())
                .markedAt(a.getMarkedAt())
                .build();
    }
}
