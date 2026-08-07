package com.Features.Student.service;

import com.Features.Student.dto.DeviceChangeLogResponse;

import java.util.List;
import java.util.UUID;

public interface AdminDeviceService {

    /** All unacknowledged device-change alerts */
    List<DeviceChangeLogResponse> getUnacknowledgedAlerts();

    /** All device-change alerts regardless of status */
    List<DeviceChangeLogResponse> getAllAlerts();

    /** Device-change history for one student */
    List<DeviceChangeLogResponse> getAlertsByStudent(UUID studentId);

    /** Mark an alert as reviewed — no action taken on the device binding */
    DeviceChangeLogResponse acknowledgeAlert(UUID alertId);

    /**
     * Reset a student's device binding.
     * Clears deviceId + phoneRegistered flag so the student must log in
     * from their phone again to re-register a new device.
     */
    void resetStudentDevice(UUID studentId);
}
