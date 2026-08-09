package com.Features.FacultyAttendance.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Sent by faculty browser when scanning the daily QR.
 * The server also reads the real IP from the HTTP request headers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacultyAttendanceScanRequest {

    @NotBlank(message = "QR token is required")
    private String qrToken;
}
