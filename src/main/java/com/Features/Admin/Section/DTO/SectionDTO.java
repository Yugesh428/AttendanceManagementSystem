package com.Features.Admin.Section.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class SectionDTO {

    private UUID id;

    @NotBlank(message = "Section name is required")
    private String name;   // A, B, C

    private String description;   // "CS Section A"

    private Integer capacity;     // 60, 80

    @NotNull(message = "Semester ID is required")
    private UUID semesterId;

    private String semesterName;  // from Semester entity

    private LocalDateTime createdAt;
}