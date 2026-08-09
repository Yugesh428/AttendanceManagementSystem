package com.Features.ModuleLeader.dto;

import lombok.*;

import java.util.UUID;

/**
 * Summary of a teacher who teaches the module leader's subject.
 * Returned by GET /api/module-leader/teachers
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherSummaryDTO {
    private UUID   teacherId;
    private String firstName;
    private String lastName;
    private String email;
    private String designation;
    private String departmentName;
    /** How many timetable slots this teacher has for the subject */
    private int    slotCount;
}
