package com.Features.Student.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceChangeLogResponse {

    private UUID          id;
    private UUID          studentId;
    private String        studentName;
    private String        studentEmail;
    private String        oldDeviceId;
    private String        newDeviceId;
    private boolean       acknowledged;
    private LocalDateTime acknowledgedAt;
    private LocalDateTime detectedAt;
}
