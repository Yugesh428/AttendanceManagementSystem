package com.Features.Admin.Classroom.DTO;

import com.Features.Admin.Classroom.model.ClassType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassroomDTO {

    private UUID id;

    @NotBlank(message = "Classroom name is required")
    @Size(min = 2, max = 100, message = "Classroom name must be between 2 and 100 characters")
    private String name;

    @NotNull(message = "Class type is required (LECTURE, TUTORIAL, PRACTICAL)")
    private ClassType classType;

    @NotNull(message = "Building ID is required")
    private UUID buildingId;

    // Populated in responses only
    private String buildingName;
    private LocalDateTime createdAt;
}
