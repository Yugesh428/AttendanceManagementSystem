package com.Features.Admin.Subject.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectResponseDTO {

    private UUID id;

    @NotBlank(message = "Subject name is required")
    @Size(min = 2, max = 100, message = "Subject name must be between 2 and 100 characters")
    private String subjectName;

    @NotBlank(message = "Subject code is required")
    @Size(min = 2, max = 20, message = "Subject code must be between 2 and 20 characters")
    private String subjectCode;

    @NotNull(message = "Course category ID is required")
    private UUID courseCategoryId;

    // Optional: for response (to show name without extra API call)
    private String courseCategoryName;
}