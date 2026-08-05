package com.Features.Admin.CourseCategory.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourseDTO {


    private UUID id;



    @NotBlank(message = "Course name is required")
    @Size(
            max = 100,
            message = "Course name must be less than 100 characters"
    )
    private String courseName;



    @NotBlank(message = "Course code is required")
    @Size(
            max = 100,
            message = "Course code must be less than 100 characters"
    )
    private String courseCode;



    @Size(
            max = 255,
            message = "Description must be less than 255 characters"
    )
    private String description;



    private LocalDateTime createdAt;



    private LocalDateTime updatedAt;
}