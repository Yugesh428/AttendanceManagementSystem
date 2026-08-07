package com.Features.Teacher.dto;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherLoginResponse {
    private String token;
    private String tokenType;
    private UUID teacherId;
    private UUID facultyId;
    private String firstName;
    private String lastName;
    private String email;
    private String designation;
    private String departmentName;
    private String role;
}
