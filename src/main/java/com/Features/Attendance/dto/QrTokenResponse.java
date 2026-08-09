package com.Features.Attendance.dto;

import com.Features.Attendance.model.QrTokenStatus;
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
public class QrTokenResponse {

    private UUID           id;
    private String         tokenValue;    // the UUID encoded in QR
    private UUID           sessionId;     // groups all rotations of same session
    private int            rotationCount;

    // Slot info
    private UUID           slotId;
    private String         subjectName;
    private String         subjectCode;
    private String         classroomName;
    private String         sectionName;
    private String         dayOfWeek;
    private String         startTime;
    private String         endTime;

    private LocalDate      sessionDate;
    private LocalDateTime  expiresAt;
    private LocalDateTime  createdAt;
    private QrTokenStatus  status;

    /**
     * Seconds remaining until this token expires.
     * Frontend uses this to show a countdown and trigger re-poll.
     */
    private long           secondsUntilExpiry;

    /**
     * The full QR URL students scan.
     * Pattern: https://yourdomain.com/attend?token=<tokenValue>
     */
    private String         qrUrl;
}
