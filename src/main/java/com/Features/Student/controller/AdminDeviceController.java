package com.Features.Student.controller;

import com.Features.Student.dto.DeviceChangeLogResponse;
import com.Features.Student.service.AdminDeviceService;
import com.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/devices")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDeviceController {

    private final AdminDeviceService adminDeviceService;

    /**
     * GET /api/admin/devices/alerts
     * Returns all UNACKNOWLEDGED device-change alerts.
     * Use this for the admin notification badge / alert panel.
     */
    @GetMapping("/alerts")
    public ResponseEntity<ApiResponse<List<DeviceChangeLogResponse>>> getUnacknowledgedAlerts() {
        List<DeviceChangeLogResponse> data = adminDeviceService.getUnacknowledgedAlerts();
        return ResponseEntity.ok(
                ApiResponse.success("Unacknowledged device alerts retrieved", data));
    }

    /**
     * GET /api/admin/devices/alerts/all
     * Returns ALL device-change alerts (acknowledged + unacknowledged).
     */
    @GetMapping("/alerts/all")
    public ResponseEntity<ApiResponse<List<DeviceChangeLogResponse>>> getAllAlerts() {
        List<DeviceChangeLogResponse> data = adminDeviceService.getAllAlerts();
        return ResponseEntity.ok(
                ApiResponse.success("All device alerts retrieved", data));
    }

    /**
     * GET /api/admin/devices/alerts/student/{studentId}
     * Full device-change history for a specific student.
     */
    @GetMapping("/alerts/student/{studentId}")
    public ResponseEntity<ApiResponse<List<DeviceChangeLogResponse>>> getAlertsByStudent(
            @PathVariable UUID studentId) {
        List<DeviceChangeLogResponse> data = adminDeviceService.getAlertsByStudent(studentId);
        return ResponseEntity.ok(
                ApiResponse.success("Student device alerts retrieved", data));
    }

    /**
     * PUT /api/admin/devices/alerts/{alertId}/acknowledge
     * Mark a specific alert as reviewed. No change to device binding.
     * Use when the admin has verified the device change was legitimate.
     */
    @PutMapping("/alerts/{alertId}/acknowledge")
    public ResponseEntity<ApiResponse<DeviceChangeLogResponse>> acknowledgeAlert(
            @PathVariable UUID alertId) {
        DeviceChangeLogResponse data = adminDeviceService.acknowledgeAlert(alertId);
        return ResponseEntity.ok(
                ApiResponse.success("Alert acknowledged", data));
    }

    /**
     * DELETE /api/admin/devices/{studentId}/reset
     * Clears the student's registered device (deviceId + phoneRegistered flag).
     * The student must log in from their phone again to re-register a new device.
     * Use when a student has changed phones and needs re-registration.
     */
    @DeleteMapping("/{studentId}/reset")
    public ResponseEntity<ApiResponse<Void>> resetDevice(@PathVariable UUID studentId) {
        adminDeviceService.resetStudentDevice(studentId);
        return ResponseEntity.ok(
                ApiResponse.success("Student device binding reset. "
                        + "Student must log in from their phone to re-register."));
    }
}
