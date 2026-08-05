package com.Features.Admin.Beacon.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeaconDTO {

    // Present in responses, absent in create requests
    private UUID id;

    @NotBlank(message = "Beacon UUID is required")
    private String uuid;

    @NotNull(message = "Major value is required")
    private Integer major;

    @NotNull(message = "Minor value is required")
    private Integer minor;

    @NotNull(message = "Classroom ID is required")
    private UUID classroomId;

    // Populated in responses only
    private String classroomName;
    private LocalDateTime createdAt;
}
