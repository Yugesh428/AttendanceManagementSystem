package com.Features.Attendance.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Sent by student's phone browser after scanning the QR code.
 *
 * qrToken   — the UUID from the QR URL parameter (?token=...)
 * deviceId  — browser fingerprint from localStorage (X-Device-Id header also accepted)
 * beaconId  — UUID of the nearest Bluetooth beacon detected by the phone browser
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceScanRequest {

    @NotBlank(message = "QR token is required")
    private String qrToken;

    @NotBlank(message = "Device ID is required")
    private String deviceId;

    @NotBlank(message = "Beacon ID is required")
    private String beaconId;
}
