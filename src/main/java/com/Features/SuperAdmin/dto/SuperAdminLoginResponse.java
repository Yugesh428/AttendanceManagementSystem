package com.Features.SuperAdmin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuperAdminLoginResponse {
    private String token;
    private String tokenType = "Bearer";
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
}
