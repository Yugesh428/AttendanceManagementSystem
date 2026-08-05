package com.Features.Admin.Semester.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SemesterDTO {

    private UUID id;

    @NotBlank
    private String name;

    private LocalDateTime createdAt;
}