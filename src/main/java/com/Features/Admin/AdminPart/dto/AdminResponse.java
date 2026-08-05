package com.Features.Admin.AdminPart.dto;

import com.Features.Admin.AdminPart.model.AdminStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String tenantName;
    private String organizationAddress;
    private String organizationCity;
    private String organizationCountry;
    private AdminStatus status;
    private LocalDateTime createdAt;
    private UUID createdBySuperAdminId;
}
