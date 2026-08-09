package com.Features.Attendance.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.UUID;

/**
 * One student enrolled in a slot's section+subject.
 * Used by teacher to see who is in their class before/after a session.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SlotStudentResponse {

    private UUID   studentId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
}
