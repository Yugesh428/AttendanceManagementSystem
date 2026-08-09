package com.Features.FacultyAttendance.dto;

import com.Features.FacultyAttendance.model.FacultyQrStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FacultyDailyQrResponse {

    private UUID            id;
    private LocalDate       qrDate;
    private String          tokenValue;
    private LocalDateTime   expiresAt;
    private FacultyQrStatus status;
    private String          generatedBy;
    private LocalDateTime   createdAt;
    private long            secondsUntilExpiry;

    /** The full QR URL for display/scanning */
    private String          qrUrl;
}
