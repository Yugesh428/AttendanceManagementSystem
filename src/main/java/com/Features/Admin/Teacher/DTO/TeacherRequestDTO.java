package com.Features.Admin.Teacher.DTO;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherRequestDTO {

    @NotBlank(message = "First name is required")
    @Size(max = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50)
    private String lastName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @Pattern(regexp = "^[0-9]{7,15}$", message = "Invalid phone number")
    private String phoneNumber;

    private String qualification;
    private String specialization;
    private String designation;

    @Min(value = 0, message = "Experience cannot be negative")
    private Integer experienceYears;

    private LocalDate dateOfBirth;
    private String gender;
    private String address;
}