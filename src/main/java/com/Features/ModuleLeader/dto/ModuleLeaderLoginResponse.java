package com.Features.ModuleLeader.dto;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleLeaderLoginResponse {

    private String token;
    private String tokenType;
    private UUID   moduleLeaderId;
    private String firstName;
    private String lastName;
    private String email;
    private UUID   subjectId;
    private String subjectName;
    private String subjectCode;
    private String role;
}
