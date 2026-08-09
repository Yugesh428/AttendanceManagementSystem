package com.Features.ModuleLeader.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModuleLeaderResponse {

    private UUID          id;
    private String        firstName;
    private String        lastName;
    private String        email;
    private String        phone;
    private UUID          subjectId;
    private String        subjectName;
    private String        subjectCode;
    private boolean       active;
    private LocalDateTime createdAt;

    /**
     * Shown ONCE on creation response — the plain-text generated password.
     * Emailed automatically. Never stored in DB. Hidden on all other responses
     * via @JsonInclude(NON_NULL).
     */
    private String        generatedPassword;
}
