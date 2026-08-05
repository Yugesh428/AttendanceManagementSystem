package com.Features.Admin.AdminPart.dto;

import com.Features.Admin.AdminPart.model.AdminStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoginResponse {
    private String token;
    private String tokenType;
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String tenantName;
    private AdminStatus status;
    private String role;
}
