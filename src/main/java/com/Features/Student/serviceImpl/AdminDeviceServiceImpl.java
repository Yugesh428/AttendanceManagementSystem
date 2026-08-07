package com.Features.Student.serviceImpl;

import com.Features.Admin.Student.model.StudentAccount;
import com.Features.Admin.Student.repository.StudentAccountRepository;
import com.Features.Student.dto.DeviceChangeLogResponse;
import com.Features.Student.model.DeviceChangeLog;
import com.Features.Student.repository.DeviceChangeLogRepository;
import com.Features.Student.service.AdminDeviceService;
import com.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminDeviceServiceImpl implements AdminDeviceService {

    private final DeviceChangeLogRepository deviceChangeLogRepository;
    private final StudentAccountRepository  accountRepository;

    // ── Alerts ────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<DeviceChangeLogResponse> getUnacknowledgedAlerts() {
        return deviceChangeLogRepository
                .findByAcknowledgedFalseOrderByDetectedAtDesc()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceChangeLogResponse> getAllAlerts() {
        return deviceChangeLogRepository.findAll()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceChangeLogResponse> getAlertsByStudent(UUID studentId) {
        return deviceChangeLogRepository
                .findByStudentIdOrderByDetectedAtDesc(studentId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ── Acknowledge ───────────────────────────────────────────────────────────

    @Override
    public DeviceChangeLogResponse acknowledgeAlert(UUID alertId) {
        log.info("[DEVICE] Acknowledging alertId='{}'", alertId);

        DeviceChangeLog log2 = deviceChangeLogRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("DeviceChangeLog", "id", alertId));

        log2.setAcknowledged(true);
        log2.setAcknowledgedAt(LocalDateTime.now());

        return mapToResponse(deviceChangeLogRepository.save(log2));
    }

    // ── Reset device binding ──────────────────────────────────────────────────

    @Override
    public void resetStudentDevice(UUID studentId) {
        log.info("[DEVICE] Resetting device for studentId='{}'", studentId);

        StudentAccount account = accountRepository
                .findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("StudentAccount", "studentId", studentId));

        account.setDeviceId(null);
        account.setDeviceRegisteredAt(null);
        account.setPhoneRegistered(false);
        accountRepository.save(account);

        log.info("[DEVICE] Device reset done for studentId='{}'", studentId);
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private DeviceChangeLogResponse mapToResponse(DeviceChangeLog d) {
        String studentName = d.getStudent().getFirstName()
                + (d.getStudent().getLastName() != null
                        ? " " + d.getStudent().getLastName() : "");
        return DeviceChangeLogResponse.builder()
                .id(d.getId())
                .studentId(d.getStudent().getId())
                .studentName(studentName)
                .studentEmail(d.getStudent().getEmail())
                .oldDeviceId(d.getOldDeviceId())
                .newDeviceId(d.getNewDeviceId())
                .acknowledged(d.isAcknowledged())
                .acknowledgedAt(d.getAcknowledgedAt())
                .detectedAt(d.getDetectedAt())
                .build();
    }
}
