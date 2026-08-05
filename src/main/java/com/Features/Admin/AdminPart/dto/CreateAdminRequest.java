package com.Features.Admin.AdminPart.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateAdminRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Password must have at least one uppercase, one lowercase, one digit, and one special character"
    )
    private String password;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phoneNumber;

    @NotBlank(message = "Tenant/Organization name is required")
    @Size(min = 2, max = 100, message = "Tenant name must be between 2 and 100 characters")
    private String tenantName;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String organizationAddress;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String organizationCity;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String organizationCountry;
}
