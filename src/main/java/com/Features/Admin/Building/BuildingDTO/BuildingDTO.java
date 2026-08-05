package com.Features.Admin.Building.BuildingDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuildingDTO {

    // Present in responses and update requests; absent in create requests
    private UUID id;

    @NotBlank(message = "Building name is required")
    @Size(min = 2, max = 100, message = "Building name must be between 2 and 100 characters")
    private String name;

    @Size(max = 255, message = "Location must not exceed 255 characters")
    private String location;

    // Populated in responses only
    private LocalDateTime createdAt;
}
